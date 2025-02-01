package com.adit.backend.infra.crawler.common;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import lombok.extern.slf4j.Slf4j;

/**
 * 크롤링 전략 추상 클래스
 */
@Slf4j
public abstract class AbstractWebCrawlingStrategy implements WebCrawlingStrategy {
	protected static final String USER_AGENT = "Mozilla/5.0";
	protected static final int TIMEOUT_SECONDS = 30;

	@Override
	public Document getDocument(String url) throws IOException {
		try {
			return Jsoup.connect(url)
				.userAgent(USER_AGENT)
				.timeout(TIMEOUT_SECONDS * 1000)
				.get();
		} catch (IOException e) {
			log.error("[Crawl] 문서 추출 실패: {}, 에러: {}", url, e.getMessage());
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
	}

	@Override
	public CrawlCompletionResponse extractContentsUsingApify(String url) {
		log.warn("[Crawl] Apify API 지원하지 않는 전략: {}", url);
		throw new UnsupportedOperationException("This strategy does not support Apify API");
	}
}
