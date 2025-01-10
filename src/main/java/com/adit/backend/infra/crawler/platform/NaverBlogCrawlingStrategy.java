package com.adit.backend.infra.crawler.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 네이버 블로그 형식의 본문을 크롤링하되,
 * se-map-info __se_link 클래스를 가진 특정 장소 정보를 함께 가져오는 예시 코드
 * [기존 TistoryCrawlingStrategy (소스 [1])를 참조하여 작성]
 */
@Component
@Slf4j
public class NaverBlogCrawlingStrategy extends AbstractWebCrawlingStrategy {

	// 네이버 블로그 내에서 본문을 감싸는 주요 div 클래스
	private static final Map<String, String> SKIN_TAGS = Map.ofEntries(
		Map.entry("main", "div.se-viewer.se-theme-default.se-main-container.post-area")
	);

	// Tistory 코드 [1]에서 사용한 chunk/batch 사이즈를 그대로 재활용
	private static final int CHUNK_SIZE = 500;
	private static final int BATCH_SIZE = 4;

	/**
	 * URL이 네이버 블로그인지 간단히 확인합니다.
	 */
	@Override
	public boolean supports(String url) {
		log.info("NaverBlogCrawlingStrategy supports? URL: {}", url);
		return url.contains("blog.naver.com");
	}

	/**
	 * HTML 문서에서 본문을 추출한 뒤,
	 * 'se-map-info __se_link'로 표시된 장소 정보를 별도로 추출하여 함께 반환합니다.
	 */
	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public String extractContents(Document document) {
		log.info("NaverBlogCrawlingStrategy: 본문 + 장소 정보 추출 시작 - URL: {}", document.location());

		// 1) 본문 추출
		String content = extractMainContent(document);
		if (content.isEmpty()) {
			log.warn("본문 추출 실패 - URL: {}", document.location());
			return null;
		}

		// 2) 장소 정보 추출
		String placeInfo = extractPlaceInfo(document);

		// 3) 본문 + 장소 정보를 합쳐 최종 결과를 생성
		String combined = content + "\n\n[PLACE INFO]\n" + placeInfo;

		// 4) 청크/배치 처리 (필요 시)
		List<String> chunks = splitIntoChunks(combined);
		return processBatchChunks(chunks).stream().collect(Collectors.joining());
	}

	/**
	 * 네이버 블로그에서 주요 본문(제목 + 내용)만 추출하는 메서드
	 */
	private String extractMainContent(Document document) {
		StringBuilder contentBuilder = new StringBuilder();
		try {
			// 제목 추출 (간단 예시)
			String title = document.select("div.blog2_container h3.se_textarea, div.blog2_container h3.title")
				.text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패 - URL: {}", document.location());
			}

			// 본문 추출
			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				log.info("본문 요소 선택 성공 - URL: {}", document.location());

				// 불필요 요소 제거
				removeUnnecessaryElements(mainContent);

				// 텍스트만 추출
				Elements textElements =
					mainContent.select("p, div:not(:has(p)), h1, h2, h3, h4, h5, h6");
				for (Element element : textElements) {
					String text = element.ownText().trim();
					// 길이 10자 이상인 텍스트만 본문으로 간주
					if (!text.isEmpty() && text.length() > 10) {
						contentBuilder.append(text).append("\n");
					}
				}
			} else {
				log.warn("본문 요소 선택 실패 - URL: {}", document.location());
			}

			return preprocessText(contentBuilder.toString());

		} catch (Exception e) {
			log.error("본문 추출 중 오류 발생 - URL: {}, 에러: {}", document.location(), e.getMessage(), e);
			return "";
		}
	}

	/**
	 * se-map-info __se_link 클래스를 갖는 요소에서 장소 정보를 추출
	 * (예: a.se-map-info.__se_link 등)
	 */
	private String extractPlaceInfo(Document document) {
		StringBuilder placeBuilder = new StringBuilder();
		try {
			// 지도/장소 정보를 나타내는 요소를 한꺼번에 선택 (a 혹은 div 등)
			Elements placeElements = document.select("a.se-map-info.__se_link, div.se-map-info.__se_link");

			if (!placeElements.isEmpty()) {
				for (Element place : placeElements) {
					placeBuilder.append(place.text().trim()).append("\n");
				}
			} else {
				placeBuilder.append("PLACE 정보가 없습니다.");
			}
		} catch (Exception e) {
			log.error("PLACE 정보 추출 중 오류 - {}", e.getMessage(), e);
			return "PLACE 정보 추출 실패";
		}
		return placeBuilder.toString().trim();
	}

	/**
	 * 네이버 블로그 본문을 감싸는 div 선택 (예: .se-main-container.post-area)
	 */
	private Elements selectContentElements(Document document) {
		for (Map.Entry<String, String> entry : SKIN_TAGS.entrySet()) {
			Elements elements = document.select(entry.getValue());
			if (!elements.isEmpty()) {
				log.info("선택자 '{}' 적용 성공 - URL: {}", entry.getValue(), document.location());
				return elements;
			}
		}
		// 기본 선택자
		return document.select("div.se-viewer");
	}

	/**
	 * 스크립트, 스타일 등 불필요한 태그 제거
	 */
	private void removeUnnecessaryElements(Element mainContent) {
		try {
			mainContent.select("script, style, iframe, button, input, textarea").remove();
			log.info("불필요 태그 제거 성공 - URL: {}", mainContent.baseUri());
		} catch (Exception e) {
			log.error("불필요 태그 제거 실패 - 에러: {}", e.getMessage(), e);
		}
	}

	/**
	 * 텍스트 전처리 (URL/특수문자 제거 등)
	 */
	private String preprocessText(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		String processedText = text.replaceAll("\\b(https?|ftp|file)://\\S+\\b", "")
			.replaceAll("<[^>]+>", "")
			.replaceAll("\\[.*?\\]", "")
			.replaceAll("\\(.*?\\)", "")
			.replaceAll("[\\p{So}\\p{Sk}]", "")
			.replaceAll("[\\r\\n]+", "\n")
			.replaceAll("\\s{2,}", " ")
			.trim();
		return processedText;
	}

	/**
	 * 공백 라인 등을 기준으로 청크 분할 (Tistory 예시 [1] 재활용)
	 */
	private List<String> splitIntoChunks(String text) {
		List<String> chunks = new ArrayList<>();
		String[] sentences = text.split("(?<=[.!?]\\s)");

		StringBuilder currentChunk = new StringBuilder();
		for (String sentence : sentences) {
			if (currentChunk.length() + sentence.length() > CHUNK_SIZE) {
				if (currentChunk.length() > 0) {
					chunks.add(currentChunk.toString().trim());
					currentChunk = new StringBuilder();
				}
			}
			currentChunk.append(sentence).append(" ");
		}
		if (currentChunk.length() > 0) {
			chunks.add(currentChunk.toString().trim());
		}
		log.info("NaverBlog 본문 청크 분할 완료 (총 개수: {})", chunks.size());
		return chunks;
	}

	/**
	 * 분할된 청크를 배치 단위로 처리 (Tistory 예시 [1] 재활용)
	 */
	private List<String> processBatchChunks(List<String> chunks) {
		List<String> results = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
			List<String> batch = chunks.subList(i, Math.min(chunks.size(), i + BATCH_SIZE));
			String combinedText = batch.stream().collect(Collectors.joining("\n"));

			if (!combinedText.trim().isEmpty()) {
				results.add(combinedText);
			}
		}
		return results;
	}
}
