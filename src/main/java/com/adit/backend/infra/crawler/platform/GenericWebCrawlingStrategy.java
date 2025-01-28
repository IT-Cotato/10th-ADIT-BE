package com.adit.backend.infra.crawler.platform;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.WebContentCrawler;
import com.adit.backend.infra.crawler.common.AbstractWebCrawlingStrategy;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import lombok.extern.slf4j.Slf4j;

/**
 * 지원하지 않는 플랫폼 크롤링 전략
 */
@Component
@Slf4j
public class GenericWebCrawlingStrategy extends AbstractWebCrawlingStrategy {

	public static final String TISTORY_URL = "tistory.com";
	public static final String NAVER_BLOG_URL = "blog.naver.com";
	public static final String BRUNCH_URL = "brunch.co.kr";
	private static final String INSTAGRAM_URL = "instagram.com";
	public static final String TEXT_TAG = "p, div:not(:has(p)), h1, h2, h3, h4, h5, h6";
	public static final String TITLE_TAG = "title, h1, h2";
	public static final String BODY_TAG = "body";
	public static final String CONTENT_TAG = ".entry-content";
	public static final String PLACE_SEPARATOR = "\n[PLACE INFO]\n";
	public static final int MINIMUM_RECOGNIZED_CHARACTER = 10;

	@Override
	public boolean supports(String url) {
		if (url == null || url.isEmpty()) {
			log.warn("[Crawl] URL이 비어있음");
			return false;
		}
		return !url.contains(TISTORY_URL)
			&& !url.contains(NAVER_BLOG_URL)
			&& !url.contains(BRUNCH_URL)
			&& !url.contains(INSTAGRAM_URL);
	}

	@Override
	@Cacheable(value = "contentCache", key = "#document.location()")
	public CrawlCompletionResponse extractContents(Document document) {
		StringBuilder contentBuilder = new StringBuilder();
		try {
			log.debug("[Crawl] 일반 웹 크롤링 시작: {}", document.location());
			WebContentCrawler.extractTitle(document, TITLE_TAG, contentBuilder);
			Element bodyElement = document.selectFirst(BODY_TAG);
			WebContentCrawler.extractBodyText(bodyElement, TEXT_TAG, MINIMUM_RECOGNIZED_CHARACTER, contentBuilder);
			String content = WebContentCrawler.preprocessText(contentBuilder.toString());
			String placeInfo = WebContentCrawler.extractPlaceInfo(document);
			String combined = content + PLACE_SEPARATOR + placeInfo;
			log.debug("[Crawl] 일반 웹 크롤링 완료");
			return WebContentCrawler.getCrawlCompletionResponse(document.select(CONTENT_TAG), combined);
		} catch (Exception e) {
			log.error("[Crawl] 본문 추출 중 오류 발생: {}", e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
	}
}
