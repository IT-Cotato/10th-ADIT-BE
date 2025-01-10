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
 * 브런치 페이지(예시) 크롤링 전략
 * TistoryCrawlingStrategy를 참조하여 작성 [1]
 */
@Component
@Slf4j
public class BrunchCrawlingStrategy extends AbstractWebCrawlingStrategy {

	// 본문을 감싸는 주요 클래스(예: .wrap_body, .text_align_left, .finish_txt)
	// 실제 사이트 구조나 스킨별로 달라질 수 있으니 상황에 따라 수정
	private static final Map<String, String> BRUNCH_TAGS = Map.ofEntries(
		Map.entry("wrap", ".wrap_body"),
		Map.entry("main", ".wrap_body_frame")
	);


	// Tistory 예시 [1]와 동일하게 청크/배치 사이즈 활용
	private static final int CHUNK_SIZE = 500;
	private static final int BATCH_SIZE = 4;

	/**
	 * 브런치 URL을 지원하는지 간단히 확인합니다.
	 */
	@Override
	public boolean supports(String url) {
		// 예: "brunch.co.kr"가 포함되어 있으면 브런치라고 가정
		return url.contains("brunch.co.kr");
	}

	/**
	 * HTML 문서에서 본문을 추출하되, <a class="place"> 태그의 텍스트를 별도로 가져와
	 * 함께 반환합니다.
	 */
	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public String extractContents(Document document) {
		log.info("BrunchCrawlingStrategy: 본문 추출 시작 - URL: {}", document.location());

		// 본문 추출
		String content = extractMainContent(document);
		if (content.isEmpty()) {
			log.warn("본문 추출 실패 - URL: {}", document.location());
			return null;
		}

		// place 정보(본문 내 a 태그 중 class=place)가 있다면 별도 추출
		String placeInfo = extractPlaceInfo(document);

		// 본문 + placeInfo 조합
		String combined = content + "\n\n[PLACE LINKS]\n" + placeInfo;

		// 필요 시 Tistory와 동일하게 청크 단위로 분리 후 배치 처리
		List<String> chunks = splitIntoChunks(combined);
		return processBatchChunks(chunks).stream().collect(Collectors.joining());
	}

	/**
	 * 브런치 페이지에서 제목과 본문 텍스트를 추출하는 메서드
	 * (wrap_body, text_align_left, finish_txt 등을 탐색)
	 */
	private String extractMainContent(Document document) {
		StringBuilder contentBuilder = new StringBuilder();

		try {
			// 제목 추출 (예: h1, h2 태그 등을 우선 탐색)
			String title = document.select("h1, h2").text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패 - URL: {}", document.location());
			}

			// 본문 선택
			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				log.info("본문 요소 선택 성공 - URL: {}", document.location());

				// 불필요한 태그 제거
				removeUnnecessaryElements(mainContent);

				// 실제 텍스트만 추출
				Elements textElements =
					mainContent.select("p, div:not(:has(p)), h1, h2, h3, h4, h5, h6");
				for (Element element : textElements) {
					String text = element.ownText().trim();
					// 임의로 길이 10 이상인 경우만 본문으로 간주
					if (!text.isEmpty() && text.length() > 10) {
						contentBuilder.append(text).append("\n");
					}
				}
			}

			return preprocessText(contentBuilder.toString());

		} catch (Exception e) {
			log.error("본문 추출 중 오류 발생 - URL: {}, 에러: {}", document.location(), e.getMessage(), e);
			return "";
		}
	}

	/**
	 * a 태그 중 class="place"를 가진 요소의 텍스트를 모아서 반환
	 */
	private String extractPlaceInfo(Document document) {
		StringBuilder placeBuilder = new StringBuilder();
		try {
			Elements placeLinks = document.select("a.place");
			if (!placeLinks.isEmpty()) {
				// 여러 개라면 모두 연결
				for (Element place : placeLinks) {
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
	 * wrap_body, text_align_left, finish_txt 등에 해당하는 요소를 우선적으로 선택
	 */
	private Elements selectContentElements(Document document) {
		for (Map.Entry<String, String> entry : BRUNCH_TAGS.entrySet()) {
			Elements elements = document.select(entry.getValue());
			if (!elements.isEmpty()) {
				log.info("선택자 '{}' 적용 성공 - URL: {}", entry.getValue(), document.location());
				return elements;
			}
		}
		// 필요한 경우 기본값 지정
		return document.select("div.wrap_body");
	}

	/**
	 * 스크립트, 스타일 등 불필요한 태그 제거
	 */
	private void removeUnnecessaryElements(Element mainContent) {
		try {
			mainContent.select("script, style, iframe, button, input, textarea").remove();
			log.info("불필요 태그 제거 완료 - URL: {}", mainContent.baseUri());
		} catch (Exception e) {
			log.error("불필요 태그 제거 실패 - 에러: {}", e.getMessage(), e);
		}
	}

	/**
	 * 텍스트 전처리 (URL, 특수문자 제거 등)
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
	 * Tistory 예시 [1]와 동일하게 본문을 청크 단위로 나누는 메서드
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
		log.info("Brunch 본문 청크 분할 완료 (총 {}개 청크)", chunks.size());
		return chunks;
	}

	/**
	 * 청크 리스트를 배치 단위(BATCH_SIZE)로 묶어 후처리
	 */
	private List<String> processBatchChunks(List<String> chunks) {
		List<String> results = new ArrayList<>();
		for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
			List<String> batch = chunks.subList(i, Math.min(chunks.size(), i + BATCH_SIZE));
			String combinedText = batch.stream().collect(Collectors.joining("\n"));

			// 배치 내 텍스트가 공백이 아닐 때만 결과에 포함
			if (!combinedText.trim().isEmpty()) {
				results.add(combinedText);
			}
		}
		return results;
	}
}
