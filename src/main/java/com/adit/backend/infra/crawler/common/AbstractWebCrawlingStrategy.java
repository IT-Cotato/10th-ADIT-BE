package com.adit.backend.infra.crawler.common;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;

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
		return Jsoup.connect(url)
			.userAgent(USER_AGENT)
			.timeout(TIMEOUT_SECONDS * 1000)
			.get();
	}

	@Override
	public CrawlCompletionResponse extractContentsUsingApify(String url) {
		throw new UnsupportedOperationException("This strategy does not support Apify API");
	}
}
