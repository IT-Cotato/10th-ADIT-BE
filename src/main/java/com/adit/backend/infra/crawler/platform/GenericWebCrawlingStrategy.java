package com.adit.backend.infra.crawler.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 플랫폼에 상관없이 기본 HTML 문서의 제목, 본문 텍스트를 크롤링하는 전략 예시
 * [TistoryCrawlingStrategy (소스 [1]) 일부 로직을 참조함]
 */
@Component
@Slf4j
public class GenericWebCrawlingStrategy extends AbstractWebCrawlingStrategy {

	private static final int CHUNK_SIZE = 500; // 본문을 나눌 최대 길이
	private static final int BATCH_SIZE = 4;   // 청크 처리 단위

	/**
	 * 모든 URL에 대해 적용 가능하도록 간단히 true 반환 (또는 필요하다면 예외 처리)
	 */
	@Override
	public boolean supports(String url) {
		return !url.contains("tistory.com") && !url.contains("blog.naver.com") && !url.contains("brunch.co.kr");
	}

	/**
	 * HTML 문서에서 제목과 본문을 추출하고, 청크/배치 처리를 수행합니다.
	 */
	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public String extractContents(Document document) {
		log.info("GenericWebCrawlingStrategy: 본문 추출 시작 - URL: {}", document.location());

		// 제목 + 본문 추출
		String content = extractMainContent(document);

		if (content.isEmpty()) {
			log.warn("본문 추출 실패 - URL: {}", document.location());
			return null;
		}

		// 청크로 분할 후 배치 단위로 처리
		List<String> chunks = splitIntoChunks(content);
		return processBatchChunks(chunks).stream().collect(Collectors.joining());
	}

	/**
	 * 제목과 본문 텍스트를 추출하는 간단한 메서드
	 */
	private String extractMainContent(Document document) {
		StringBuilder contentBuilder = new StringBuilder();
		try {
			// 제목 추출 (document의 <title> 태그나 주요 헤딩 태그 등)
			String title = document.select("title, h1, h2").first() != null
				? document.select("title, h1, h2").first().text()
				: "";
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패 - URL: {}", document.location());
			}

			// 본문(단순히 <body> 내부 텍스트) 추출
			Element bodyElement = document.selectFirst("body");
			if (bodyElement != null) {
				// 스크립트, 스타일 태그 등 제거
				removeUnnecessaryElements(bodyElement);

				// 문단, div, 헤딩 태그만 대상으로 텍스트 추출
				Elements textElements = bodyElement.select("p, div:not(:has(p)), h1, h2, h3, h4, h5, h6");
				for (Element element : textElements) {
					String text = element.ownText().trim();
					if (!text.isEmpty() && text.length() > 10) {
						contentBuilder.append(text).append("\n");
					}
				}
			} else {
				log.warn("body 태그 선택 실패 - URL: {}", document.location());
			}

			return preprocessText(contentBuilder.toString());

		} catch (Exception e) {
			log.error("본문 추출 중 오류 - URL: {}, 에러: {}", document.location(), e.getMessage(), e);
			return "";
		}
	}

	/**
	 * 스크립트, 스타일 등 불필요한 요소를 제거
	 */
	private void removeUnnecessaryElements(Element mainContent) {
		try {
			mainContent.select("script, style, iframe, button, input, textarea").remove();
			log.info("불필요한 태그 제거 완료 - URL: {}", mainContent.baseUri());
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
	 * [1] 와 유사하게 본문을 청크 단위로 분할
	 */
	private List<String> splitIntoChunks(String text) {
		List<String> chunks = new ArrayList<>();
		String[] sentences = text.split("(?<=[.!?]\\s)");

		StringBuilder currentChunk = new StringBuilder();
		for (String sentence : sentences) {
			// 청크가 너무 길어지면, 하나의 청크로 추가 후 새로 생성
			if (currentChunk.length() + sentence.length() > CHUNK_SIZE) {
				if (currentChunk.length() > 0) {
					chunks.add(currentChunk.toString().trim());
					currentChunk = new StringBuilder();
				}
			}
			currentChunk.append(sentence).append(" ");
		}

		// 마지막에 남은 텍스트가 있으면 청크로 추가
		if (currentChunk.length() > 0) {
			chunks.add(currentChunk.toString().trim());
		}

		log.info("본 데이터를 청크로 분할 완료 (총 {}개 청크)", chunks.size());
		return chunks;
	}

	/**
	 * [1] 처럼 분할된 청크를 배치 단위로 묶어 후처리
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
