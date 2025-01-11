package com.adit.backend.infra.crawler.common;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;

public interface WebCrawlingStrategy {
	boolean supports(String url);

	Document getDocument(String url) throws IOException;

	CrawlCompletionResponse extractContents(Document document);
}
