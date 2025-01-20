package com.adit.backend.domain.ai.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record CrawlCompletionResponse(String crawlingData, List<String> imageSrcList
) {
	public static CrawlCompletionResponse of(String crawlingData, List<String> imageSrcList) {
		return CrawlCompletionResponse.builder()
			.crawlingData(crawlingData)
			.imageSrcList(imageSrcList)
			.build();
	}
}
