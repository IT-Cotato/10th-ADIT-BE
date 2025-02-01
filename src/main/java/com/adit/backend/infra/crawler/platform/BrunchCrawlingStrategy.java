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
 * 브런치스토리 플랫폼 크롤링 전략
 */
@Component
@Slf4j
public class BrunchCrawlingStrategy extends AbstractWebCrawlingStrategy {

	public static final String BRUNCH_URL = "brunch.co.kr";
	public static final String TEXT_TAG = "p, div:not(:has(p)), h1, h2, h3, h4, h5, h6";
	public static final String TITLE_TAG = "h1, h2";
	public static final String DEFAULT_CONTENT_TAG = "div.wrap_body";
	public static final String PLACE_SEPARATOR = "\n[PLACE INFO]\n";
	public static final int MINIMUM_RECOGNIZED_CHARACTER = 10;
	private static final Map<String, String> BRUNCH_CONTENT_TAGS = Map.ofEntries(
		Map.entry("wrap", ".wrap_body"),
		Map.entry("main", ".wrap_body_frame")
	);

	@Override
	public boolean supports(String url) {
		if (url == null || url.isEmpty()) {
			log.warn("[Crawl] URL이 비어있음");
			return false;
		}
		return url.contains(BRUNCH_URL);
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		StringBuilder contentBuilder = new StringBuilder();

		try {
			log.debug("[Crawl] 브런치 크롤링 시작: {}", document.location());
			WebContentCrawler.extractTitle(document, TITLE_TAG, contentBuilder);
			Elements contentElements = selectContentElements(document);
			if (!contentElements.isEmpty()) {
				Element mainContent = contentElements.first();
				WebContentCrawler.extractBodyText(mainContent, TEXT_TAG, MINIMUM_RECOGNIZED_CHARACTER, contentBuilder);
			}
			String content = WebContentCrawler.preprocessText(contentBuilder.toString());
			String placeInfo = WebContentCrawler.extractPlaceInfo(document);
			String combined = content + PLACE_SEPARATOR + placeInfo;
			log.debug("[Crawl] 브런치 크롤링 완료");
			return WebContentCrawler.getCrawlCompletionResponse(contentElements, combined);
		} catch (Exception e) {
			log.error("[Crawl] 브런치 크롤링 실패: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
	}

	private Elements selectContentElements(Document document) {
		for (Map.Entry<String, String> entry : BRUNCH_CONTENT_TAGS.entrySet()) {
			Elements elements = document.select(entry.getValue());
			if (!elements.isEmpty()) {
				log.debug("[Crawl] 브런치 스킨 선택자 매칭 완료: {}", entry.getValue());
				return elements;
			}
		}
		log.debug("[Crawl] 브런치 기본 선택자 사용");
		return document.select(DEFAULT_CONTENT_TAG);
	}
}
