package com.adit.backend.infra.crawler.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.infra.crawler.common.WebCrawlingStrategy;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WebContentCrawlingService {
	private final List<WebCrawlingStrategy> crawlingStrategies;

	/**
	 * 크롤링 비동기 처리
	 */
	@Async("crawlingTaskExecutor")
	public CompletableFuture<CrawlCompletionResponse> crawlAsync(String url) {
		try {
			WebCrawlingStrategy strategy = findStrategy(url);
			Document document = strategy.getDocument(url);
			CrawlCompletionResponse contents = strategy.extractContents(document);
			log.info("[크롤링 완료] : {}", url);
			return CompletableFuture.completedFuture(contents);
		} catch (Exception e) {
			log.error("[크롤링 실패] : {}", url);
			throw new CrawlingException(GlobalErrorCode.CRAWLING_FAILED);
		}
	}

	/**
	 * 플랫폼 구별
	 */
	private WebCrawlingStrategy findStrategy(String url) {
		return crawlingStrategies.stream()
			.filter(strategy -> strategy.supports(url))
			.findFirst()
			.orElseThrow(() -> new CrawlingException(GlobalErrorCode.PLATFORM_NOT_SUPPORTED));
	}
}