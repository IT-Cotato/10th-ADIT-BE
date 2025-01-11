package com.adit.backend.domain.ai.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.Service.WebCrawlingService;
import com.adit.backend.infra.crawler.exception.CrawlingException;

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
			throw new CrawlingException(GlobalErrorCode.INVALID_URL);
		}
	}

	private CrawlCompletionResponse handleError(Throwable throwable) {
		log.error("컨텐츠 추출 실패", throwable);
		return null;
	}
}