package com.adit.backend.infra.crawler.platform;

import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.WebContentCrawler;
import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import lombok.extern.slf4j.Slf4j;


/**
 * 티스토리 플랫폼 크롤링 전략
 */
@Component
@Slf4j
public class TistoryCrawlingStrategy extends AbstractWebCrawlingStrategy {

	public static final String TISTORY_URL = "tistory.com";
	public static final String TEXT_TAG = "p, div:not(:has(p)), h1, h2, h3, h4, h5, h6";
	public static final String TITLE_TAG = ".title, .article-header h1, .post-header h1";
	public static final String DEFAULT_CONTENT_TAG = ".entry-content";
	public static final int MINIMUM_RECOGNIZED_CHARACTER = 10;
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

	@Override
	public boolean supports(String url) {
		return url.contains(TISTORY_URL);
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		StringBuilder contentBuilder = new StringBuilder();
		try {
			WebContentCrawler.extractTitle(document, TITLE_TAG, contentBuilder);
			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				log.info("[본문 요소 추출] : {}", mainContent.cssSelector());
				WebContentCrawler.extractBodyText(mainContent, TEXT_TAG, MINIMUM_RECOGNIZED_CHARACTER, contentBuilder);
			}
			String content = WebContentCrawler.preprocessText(contentBuilder.toString());
			return WebContentCrawler.getCrawlCompletionResponse(contentElements, content);
		} catch (Exception e) {
			log.error("[본문 추출 중 오류 발생] : {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
	}

	private Elements selectContentElements(Document document) {
		for (Map.Entry<String, String> entry : SKIN_TAGS.entrySet()) {
			Elements elements = document.select(entry.getValue());
			if (!elements.isEmpty()) {
				log.info("[스킨 선택자 추출 완료] : {}", entry.getValue());
				return elements;
			}
		}
		log.info("[기본 선택자 추출 완료]");
		return document.select(DEFAULT_CONTENT_TAG);
	}
}
