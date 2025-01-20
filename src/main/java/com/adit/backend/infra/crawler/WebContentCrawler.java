package com.adit.backend.infra.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebContentCrawler {

	private static final int CHUNK_SIZE = 500;
	private static final int BATCH_SIZE = 4;
	private static final String PLACE_TAG = "a.place, a.se-map-info.__se_link, div.se-map-info.__se_link";
	private static final String UNNECESSARY_TAGS = "script, style, button, input, textarea, "
		+ "div.another_category, dic.category, div.recommend_list, div.profile, div#postListBottom, div.wrap_postcomment, "
		+ "div.item_type_opengraph, div.lnb,div.search, div.search-tab-all div.inner50";


	/**
	 * 텍스트 전처리: HTML 태그, URL, 특수문자 등을 제거
	 */
	public static String preprocessText(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		return text
			.replaceAll("\\{\\{[^}]+\\}\\}", "")
			.replaceAll("저작자[^\\n]*변경[^\\n]*불가", "")
			.replaceAll("공지\\s*목록[^\\n]*\\n?", "")
			.replaceAll("댓글쓰기[^\\n]*다음", "")
			.replaceAll("URL[^\\n]*신고하기", "")
			.replaceAll("\\[.*?\\]", "")
			.replaceAll("\\(.*?\\)", "")
			.replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]", "")
			.replaceAll("[\\u2600-\\u27BF]", "")
			.replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]", "")
			.replaceAll("\\b(https?|ftp|file)://\\S+\\b", "")
			.replaceAll("<[^>]+>", "")
			.replaceAll("#\\w+", "")
			.replaceAll("\\[.*?\\]", "")
			.replaceAll("\\(.*?\\)", "")
			.replaceAll("[\\p{So}\\p{Sk}]", "")
			.replaceAll("[\\r\\n]+", "\n")
			.replaceAll("\\s{2,}", " ")
			.trim();
	}

	/**
	 * 불필요한 태그 제거
	 */
	public static void removeUnnecessaryElements(Element mainContent) {
		try {
			mainContent.select(UNNECESSARY_TAGS).remove();
			log.info("[불필요 태그 제거 완료]");
		} catch (Exception e) {
			log.error("[불필요 태그 제거 실패]: {}", e.getMessage(), e);
		}
	}

	/**
	 * 본문을 일정한 문장 단위(CHUNK_SIZE)로 분리
	 */
	public static List<String> splitIntoChunks(String text) {
		List<String> chunks = new ArrayList<>();
		String[] sentences = text.split("(?<=[.!?]\\s)");
		StringBuilder currentChunk = new StringBuilder();
		for (String sentence : sentences) {
			if (currentChunk.length() + sentence.length() > CHUNK_SIZE && !currentChunk.isEmpty()) {
					chunks.add(currentChunk.toString().trim());
					currentChunk.setLength(0);
				}

			currentChunk.append(sentence).append(" ");
		}
		if (!currentChunk.isEmpty()) {
			chunks.add(currentChunk.toString().trim());
		}
		log.info("[본문을 청크로 분할] 개수: {}", chunks.size());
		return chunks;
	}

	/**
	 * 나눈 청크를 일정 개수(BATCH_SIZE)씩 묶어서 합침
	 */
	public static List<String> processBatchChunks(List<String> chunks) {
		List<String> results = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
			List<String> batch = chunks.subList(i, Math.min(chunks.size(), i + BATCH_SIZE));
			String combinedText = String.join("\n", batch);
			if (!combinedText.trim().isEmpty()) {
				results.add(combinedText);
			}
		}
		return results;
	}

	/**
	 * 이미지 주소(src) 리스트 추출
	 */
	public static List<String> extractImageSrcList(Elements elements) {
		if (elements == null) {
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
		List<String> imageSrcList = new ArrayList<>();
		Elements imgElements = elements.select("img");
		for (Element img : imgElements) {
			String highResUrl = img.attr("data-lazy-src");
			if (highResUrl.isEmpty()) {
				highResUrl = img.attr("data-origin");
			}
			if (highResUrl.isEmpty()) {
				highResUrl = img.attr("src");
			}
			if (highResUrl.contains("?type=")) {
				highResUrl = highResUrl.split("\\?type=")[0];
			}
			if (!highResUrl.isEmpty()) {
				imageSrcList.add(highResUrl + "?type=w966");
			}
		}
		return imageSrcList;
	}

	/**
	 * 장소 정보 추출
	 */
	public static String extractPlaceInfo(Document document) {
		StringBuilder placeBuilder = new StringBuilder();
		try {
			Elements placeElements = document.select(PLACE_TAG);
			for (Element place : placeElements) {
				String text = place.text().trim();
				if (!text.isEmpty()) {
					placeBuilder.append(text).append("\n");
				}
			}
		} catch (Exception e) {
			log.error("[장소 추출 중 오류 발생] : {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
		log.info("[장소 추출 완료]");
		return placeBuilder.toString().trim();
	}

	/**
	 * 크롤링 결과(CrawlCompletionResponse)를 생성
	 */
	public static CrawlCompletionResponse getCrawlCompletionResponse(Elements elements, String contents) {
		if (contents.isEmpty()) {
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
		log.debug("추출된 원본 컨텐츠 (전체 길이: {}자): {}", contents.length(), contents);
		List<String> chunks = splitIntoChunks(contents);
		List<String> imageSrcList = extractImageSrcList(elements);
		return CrawlCompletionResponse.of(
			String.join("", processBatchChunks(chunks)),
			imageSrcList
		);
	}

	/**
	 * iframe이 있는 블로그(네이버 등) 내부의 문서를 가져오기
	 */
	public static Document getIframeDocument(Document outerDoc, String iframeTag, String baseUrl) throws IOException {
		Element iframe = outerDoc.selectFirst(iframeTag);
		if (iframe == null) {
			log.warn("[iframe 추출 중 오류] : iframe을 찾지 못했습니다");
			return outerDoc;
		}
		String src = iframe.attr("src");
		if (src.isBlank()) {
			log.warn("[iframe 추출 중 오류] : iframe src가 비어 있습니다");
			return outerDoc;
		}
		String iframeUrl = src.startsWith("http") ? src : baseUrl + src;
		log.info("[iframe URL 추출 완료] : {}", iframeUrl);
		return Jsoup.connect(iframeUrl).userAgent("Mozilla/5.0").get();
	}

	/**
	 * 공통 제목 추출
	 */
	public static void extractTitle(Document document, String titleTag, StringBuilder contentBuilder) {
		String title = document.select(titleTag).text();
		if (!title.isEmpty()) {
			contentBuilder.append("제목: ").append(title).append("\n\n");
			log.info("[제목 추출 완료] : {}", title);
		} else {
			log.warn("[제목 추출 실패] : {}", document.location());
		}
	}

	/**
	 * 공통 본문 추출
	 */
	public static void extractBodyText(Element mainContent,
		String textTag,
		int minRecognizedChar,
		StringBuilder contentBuilder) {
		if (mainContent == null) {
			return;
		}
		removeUnnecessaryElements(mainContent);
		Elements textElements = mainContent.select(textTag);
		for (Element element : textElements) {
			String text = element.text().trim();
			if (text.length() > minRecognizedChar) {
				contentBuilder.append(text).append("\n");
			}
		}
	}
}
