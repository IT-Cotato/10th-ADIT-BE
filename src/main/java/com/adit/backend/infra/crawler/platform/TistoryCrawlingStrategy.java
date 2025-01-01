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

@Component
@Slf4j
public class TistoryCrawlingStrategy extends AbstractWebCrawlingStrategy {

	private static final Map<String, String> SKIN_TAGS;
	private static final int CHUNK_SIZE = 500; // 본문을 나눌 최대 길이
	private static final int BATCH_SIZE = 4;  // 청크 처리 단위

	static {
		SKIN_TAGS = Map.ofEntries(Map.entry("default", ".entry-content, #content, .article_view"),
			Map.entry("modern", ".content-wrapper"), Map.entry("bookclub", ".post-list.tab-ui"),
			Map.entry("odyssey", ".article-content"), Map.entry("skinview", ".skin_view"),
			Map.entry("blogview", ".blogview-content"), Map.entry("postcontent", "#post-content"),
			Map.entry("useless_margin", ".tt_article_useless_p_margin"), Map.entry("articlebody", ".article-body"),
			Map.entry("blogpost", ".blog-post"), Map.entry("contentarea", ".content-area"),
			Map.entry("postwrapper", ".post-wrapper"), Map.entry("maincontent", ".main-content"),
			Map.entry("articleinner", ".article-inner"));
	}

	/**
	 * Tistory URL을 지원하는지 확인합니다.
	 *
	 * @param url 크롤링 대상 URL
	 * @return Tistory URL 여부를 반환
	 */
	@Override
	public boolean supports(String url) {
		log.info("URL 지원 여부 확인: {}", url);
		return url.contains("tistory.com");
	}

	/**
	 * HTML 문서에서 본문 내용을 추출합니다. 캐싱을 적용하여 동일한 URL 요청 시 캐시된 결과를 반환합니다.
	 *
	 * @param document Jsoup Document 객체
	 * @return 추출된 본문 텍스트
	 */
	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public String extractContents(Document document) {
		log.info("TistoryCrawlingStrategy: 본문 컨텐츠 추출 시작 - URL: {}", document.location());

		String content = extractMainContent(document);

		if (content.isEmpty()) {
			log.warn("본문 추출 실패 - URL: {}", document.location());
			return null;
		}

		log.debug("추출된 원본 컨텐츠 (전체 길이: {}자): {}", content.length(), content);

		List<String> chunks = splitIntoChunks(content);
		return processBatchChunks(chunks).stream().collect(Collectors.joining());
	}

	/**
	 * HTML 문서에서 제목, 본문 텍스트, 태그 정보를 추출하여 반환합니다.
	 *
	 * @param document Jsoup Document 객체
	 * @return 전처리된 본문 텍스트
	 */
	private String extractMainContent(Document document) {
		StringBuilder contentBuilder = new StringBuilder();

		try {
			// 제목 추출
			String title = document.select(".title, .article-header h1, .post-header h1").text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패 - URL: {}", document.location());
			}

			// 본문 선택자 적용 및 요소 추출
			Elements contentElements = selectContentElements(document);

			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				log.info("본문 요소 선택 성공 - 선택자: {}, URL: {}", mainContent.cssSelector(), document.location());

				removeUnnecessaryElements(mainContent);

				Elements textElements = mainContent.select("p, div:not(:has(p)), h1, h2, h3, h4, h5, h6");

				for (Element element : textElements) {
					String text = element.ownText().trim();
					if (!text.isEmpty() && text.length() > 10) {
						contentBuilder.append(text).append("\n");
						log.debug("텍스트 추출 성공 (길이: {}): {}", text.length(), text);
					}
				}
			} else {
				log.warn("본문 요소 선택 실패 - URL: {}", document.location());
			}

			// 태그 정보 추가
			appendTags(document, contentBuilder);

			log.info("추출된 본문 길이: {} 자 - URL: {}", contentBuilder.length(), document.location());

			return preprocessText(contentBuilder.toString());

		} catch (Exception e) {
			log.error("본문 추출 중 오류 발생 - URL: {}, 에러 메시지: {}", document.location(), e.getMessage(), e);
			return "";
		}
	}

	/**
	 * 스킨별로 적합한 CSS 선택자를 적용하여 본문 요소를 선택합니다.
	 *
	 * @param document Jsoup Document 객체
	 * @return 선택된 Elements 객체
	 */
	private Elements selectContentElements(Document document) {
		for (Map.Entry<String, String> entry : SKIN_TAGS.entrySet()) {
			Elements elements = document.select(entry.getValue());
			if (!elements.isEmpty()) {
				log.info("스킨 '{}'에 해당하는 선택자를 사용합니다. 선택자: {}, URL: {}", entry.getKey(), entry.getValue(),
					document.location());
				return elements;
			}
		}
		log.warn("적합한 스킨 선택자를 찾지 못했습니다. 기본 선택자를 사용합니다 - URL: {}", document.location());
		return document.select(".entry-content"); // 기본 선택자
	}

	/**
	 * 불필요한 HTML 요소를 제거합니다.
	 *
	 * @param mainContent 본문 요소 Element 객체
	 */
	private void removeUnnecessaryElements(Element mainContent) {
		try {
			mainContent.select(
				"script, style, .container_postbtn, .article_toolbar, .sns_widget, .another_category, .tags").remove();
			log.info("불필요한 요소 제거 성공 - URL: {}", mainContent.baseUri());
		} catch (Exception e) {
			log.error("불필요한 요소 제거 실패 - URL: {}, 에러 메시지: {}", mainContent.baseUri(), e.getMessage(), e);
		}
	}

	/**
	 * 태그 정보를 추출하여 본문에 추가합니다.
	 *
	 * @param document       Jsoup Document 객체
	 * @param contentBuilder 본문 텍스트를 저장하는 StringBuilder 객체
	 */
	private void appendTags(Document document, StringBuilder contentBuilder) {
		Elements tags = document.select(".tags a");
		if (!tags.isEmpty()) {
			contentBuilder.append("\n태그: ");
			tags.forEach(tag -> contentBuilder.append(tag.text()).append(", "));
			contentBuilder.setLength(contentBuilder.length() - 2); // 마지막 ", " 제거
			log.info("태그 정보 추가 성공 - 태그 개수: {}, URL: {}", tags.size(), document.location());
		} else {
			log.warn("태그 정보가 없습니다 - URL: {}", document.location());
		}
	}

	/**
	 * 텍스트 전처리 작업을 수행합니다. (URL 제거, 특수문자 정리 등)
	 *
	 * @param text 원본 텍스트
	 * @return 전처리된 텍스트
	 */
	private String preprocessText(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		String processedText = text.replaceAll("\\b(https?|ftp|file)://\\S+\\b", "")
			.replaceAll("\\b[\\w-]+\\.tistory\\.com\\b", "")
			.replaceAll("<[^>]+>", "")
			.replaceAll("\\[.*?\\]", "")
			.replaceAll("\\(.*?\\)", "")
			.replaceAll("[\\p{So}\\p{Sk}]", "")
			.replaceAll("[\\r\\n]+", "\n")
			.replaceAll("\\s{2,}", " ").trim();

		log.debug("텍스트 전처리 완료 (길이 감소 전/후): {}/{}", text.length(), processedText.length());
		return processedText;
	}

	/**
	 * 본문을 청크 단위로 나눕니다.
	 *
	 * @param text 원본 텍스트
	 * @return 나뉜 청크 리스트
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

		log.info("본문을 청크로 분할 완료 - 총 청크 개수: {}, URL 길이: {}", chunks.size(), text.length());

		return chunks;
	}

	/**
	 * 청크를 배치 단위로 처리하여 ContentResponse 객체를 생성합니다.
	 *
	 * @param chunks 나뉜 청크 리스트
	 * @return 처리된 청크 리스트 결과물
	 */
	private List<String> processBatchChunks(List<String> chunks) {
		List<String> results = new ArrayList<>();

		for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
			List<String> batch = chunks.subList(i, Math.min(chunks.size(), i + BATCH_SIZE));
			String combinedText = batch.stream().collect(Collectors.joining("\n"));

			if (!combinedText.trim().isEmpty()) {
				results.add(combinedText); // 실제 텍스트 추가
				log.debug("청크 배치 처리 완료 - 배치 크기: {}, 텍스트 길이: {}", batch.size(), combinedText.length());
			}
		}

		return results;
	}
}
