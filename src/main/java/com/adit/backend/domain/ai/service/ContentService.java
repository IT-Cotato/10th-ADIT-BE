package com.adit.backend.domain.ai.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.exception.CrawlingException;
import com.adit.backend.infra.crawler.service.WebCrawlingService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentService {
	private final WebCrawlingService crawlingService;

	public CompletableFuture<CrawlCompletionResponse> extractContents(String url) {
		validateUrl(url);
		return crawlingService.crawlAsync(url);
	}

	private void validateUrl(String url) {
		if (!StringUtils.hasText(url) || !url.startsWith("http")) {
			log.error("[검증되지 않은 URL] : {}", url);
			throw new CrawlingException(GlobalErrorCode.INVALID_URL);
		}
	}
}