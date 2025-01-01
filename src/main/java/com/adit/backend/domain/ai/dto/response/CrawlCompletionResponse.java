package com.adit.backend.domain.ai.dto.response;

public record CrawlCompletionResponse(String crawlingData
) {
	public static CrawlCompletionResponse of(String crawlingData) {
		return new CrawlCompletionResponse(crawlingData);
	}
}
