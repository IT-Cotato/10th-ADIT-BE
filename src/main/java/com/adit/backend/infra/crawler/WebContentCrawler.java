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

	public static void removeUnnecessaryElements(Element mainContent) {
		try {
			mainContent.select(UNNECESSARY_TAGS).remove();
			log.debug("[Crawl] 불필요 태그 제거 완료");
		} catch (Exception e) {
			log.error("[Crawl] 불필요 태그 제거 실패: {}", e.getMessage(), e);
			throw new CrawlingException(GlobalErrorCode.TEXT_PREPROCESSING_FAILED);
		}
	}

	public static List<String> splitIntoChunks(String text) {
		try {
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
			log.debug("[Crawl] 본문 청크 분할 완료. 총 {}개", chunks.size());
			return chunks;
		} catch (Exception e) {
			log.error("[Crawl] 청크 처리 실패: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CHUNK_PROCESSING_FAILED);
		}
	}

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

	public static List<String> extractImageSrcList(Elements elements) {
		if (elements == null) {
			log.error("[Crawl] 이미지 추출을 위한 요소가 null");
			throw new CrawlingException(GlobalErrorCode.IMAGE_EXTRACTION_FAILED);
		}
		try {
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
		} catch (Exception e) {
			log.error("[Crawl] 이미지 URL 추출 실패: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.IMAGE_EXTRACTION_FAILED);
		}
	}

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
			log.error("[Crawl] 장소 정보 추출 실패: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.PLACE_EXTRACTION_FAILED);
		}
		log.debug("[Crawl] 장소 정보 추출 완료");
		return placeBuilder.toString().trim();
	}

	public static CrawlCompletionResponse getCrawlCompletionResponse(Elements elements, String contents) {
		if (contents.isEmpty()) {
			log.error("[Crawl] 크롤링 컨텐츠 없음");
			throw new CrawlingException(GlobalErrorCode.CONTENT_EMPTY);
		}
		log.debug("[Crawl] 원본 컨텐츠 추출 완료 ({}자): {}", contents.length(), contents);
		List<String> chunks = splitIntoChunks(contents);
		List<String> imageSrcList = extractImageSrcList(elements);
		return CrawlCompletionResponse.of(
			String.join("", processBatchChunks(chunks)),
			imageSrcList
		);
	}

	public static Document getIframeDocument(Document outerDoc, String iframeTag, String baseUrl) throws IOException {
		try {
			Element iframe = outerDoc.selectFirst(iframeTag);
			if (iframe == null) {
				log.warn("[Crawl] iframe 요소 없음");
				return outerDoc;
			}
			String src = iframe.attr("src");
			if (src.isBlank()) {
				log.warn("[Crawl] iframe src 속성 없음");
				return outerDoc;
			}
			String iframeUrl = src.startsWith("http") ? src : baseUrl + src;
			log.debug("[Crawl] iframe URL 추출 완료: {}", iframeUrl);
			return Jsoup.connect(iframeUrl).userAgent("Mozilla/5.0").get();
		} catch (Exception e) {
			log.error("[Crawl] iframe 처리 실패: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.IFRAME_CRAWLING_FAILED);
		}
	}

	public static void extractTitle(Document document, String titleTag, StringBuilder contentBuilder) {
		try {
			String title = document.select(titleTag).text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.debug("[Crawl] 제목 추출 완료: {}", title);
			} else {
				log.warn("[Crawl] 제목 추출 실패: {}", document.location());
				throw new CrawlingException(GlobalErrorCode.TITLE_EXTRACTION_FAILED);
			}
		} catch (Exception e) {
			log.error("[Crawl] 제목 추출 중 오류 발생: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.TITLE_EXTRACTION_FAILED);
		}
	}

	public static void extractBodyText(Element mainContent, String textTag, int minRecognizedChar,
		StringBuilder contentBuilder) {
		if (mainContent == null) {
			log.error("[Crawl] 본문 요소가 null");
			throw new CrawlingException(GlobalErrorCode.BODY_EXTRACTION_FAILED);
		}
		try {
			removeUnnecessaryElements(mainContent);
			Elements textElements = mainContent.select(textTag);
			for (Element element : textElements) {
				String text = element.text().trim();
				if (text.length() > minRecognizedChar) {
					contentBuilder.append(text).append("\n");
				}
			}
		} catch (Exception e) {
			log.error("[Crawl] 본문 추출 실패: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.BODY_EXTRACTION_FAILED);
		}
	}
}
