package com.adit.backend.infra.crawler.platform;

import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;
import com.adit.backend.infra.crawler.util.CrawlingUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TistoryCrawlingStrategy extends AbstractWebCrawlingStrategy {

	private static final Map<String, String> SKIN_TAGS = Map.ofEntries(
		Map.entry("default", ".entry-content, #content, .article_view"),
		Map.entry("modern", ".content-wrapper"),
		Map.entry("bookclub", ".post-list.tab-ui"),
		Map.entry("odyssey", ".article-content"),
		Map.entry("skinview", ".skin_view"),
		Map.entry("blogview", ".blogview-content"),
		Map.entry("postcontent", "#post-content"),
		Map.entry("useless_margin", ".tt_article_useless_p_margin"),
		Map.entry("articlebody", ".article-body"),
		Map.entry("blogpost", ".blog-post"),
		Map.entry("contentarea", ".content-area"),
		Map.entry("postwrapper", ".post-wrapper"),
		Map.entry("maincontent", ".main-content"),
		Map.entry("articleinner", ".article-inner"),
		Map.entry("areaview", ".area-view")
	);

	private final StringBuilder contentBuilder = new StringBuilder();

	@Override
	public boolean supports(String url) {
		log.info("URL 지원 여부 확인: {}", url);
		return url.contains("tistory.com");
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		String content = extractMainContent(document);
		return CrawlingUtil.getCrawlCompletionResponse(selectContentElements(document), content);
	}

	private String extractMainContent(Document document) {
		try {
			String title = document.select(".title, .article-header h1, .post-header h1").text();
			if (!title.isEmpty()) {
				contentBuilder.append("제목: ").append(title).append("\n\n");
				log.info("제목 추출 성공: {}", title);
			} else {
				log.warn("제목 추출 실패 - URL: {}", document.location());
			}

			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				log.info("본문 요소 선택 성공 - 선택자: {}, URL: {}", mainContent.cssSelector(), document.location());

				CrawlingUtil.removeUnnecessaryElements(mainContent);
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

			log.info("추출된 본문 길이: {} 자 - URL: {}", contentBuilder.length(), document.location());
			return CrawlingUtil.preprocessText(contentBuilder.toString());

		} catch (Exception e) {
			log.error("본문 추출 중 오류 발생 - URL: {}, 에러 메시지: {}", document.location(), e.getMessage(), e);
			return "";
		}
	}

	private Elements selectContentElements(Document document) {
		for (Map.Entry<String, String> entry : SKIN_TAGS.entrySet()) {
			Elements elements = document.select(entry.getValue());
			if (!elements.isEmpty()) {
				log.info("스킨 '{}'에 해당하는 선택자를 사용합니다. 선택자: {}, URL: {}", entry.getKey(), entry.getValue(), document.location());
				return elements;
			}
		}
		log.warn("적합한 스킨 선택자를 찾지 못했습니다. 기본 선택자를 사용합니다 - URL: {}", document.location());
		return document.select(".entry-content");
	}
}