package com.adit.backend.infra.crawler.Service;

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
public class WebCrawlingService {
	private final List<WebCrawlingStrategy> crawlingStrategies;

	@Async("crawlingTaskExecutor")
	public CompletableFuture<CrawlCompletionResponse> crawlAsync(String url) {
		try {
			WebCrawlingStrategy strategy = findStrategy(url);
			Document document = strategy.getDocument(url);
			CrawlCompletionResponse contents = strategy.extractContents(document);
			log.info("크롤링 완료: URL={}", url);
			return CompletableFuture.completedFuture(contents);
		} catch (Exception e) {
			log.error("크롤링 실패: {}", url, e);
			return CompletableFuture.completedFuture(null);
		}
	}

	private WebCrawlingStrategy findStrategy(String url) {
		return crawlingStrategies.stream()
			.filter(strategy -> strategy.supports(url))
			.findFirst()
			.orElseThrow(() -> new CrawlingException(GlobalErrorCode.PLATFORM_NOT_SUPPORTED));
	}
}