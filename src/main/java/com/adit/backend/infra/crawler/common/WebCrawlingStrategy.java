package com.adit.backend.infra.crawler.common;

import java.io.IOException;

import org.jsoup.nodes.Document;

public interface WebCrawlingStrategy {
	boolean supports(String url);

	Document getDocument(String url) throws IOException;

	String  extractContents(Document document);
}
