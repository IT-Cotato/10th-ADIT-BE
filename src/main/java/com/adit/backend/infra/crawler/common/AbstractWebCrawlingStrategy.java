package com.adit.backend.infra.crawler.common;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import lombok.extern.slf4j.Slf4j;

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

	protected String cleanText(String text) {
		return text != null ? text.trim().replaceAll("\\s+", " ") : null;
	}

	protected boolean isValidContent(String name, String location) {
		return name != null && !name.isEmpty() && location != null && !location.isEmpty();
	}
}
