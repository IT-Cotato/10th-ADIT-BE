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
import com.adit.backend.infra.crawler.platform.InstagramCrawlingStrategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WebContentCrawlingService {
	private static final String INSTAGRAM_URL = "instagram.com";
	private final List<WebCrawlingStrategy> crawlingStrategies;
	private final InstagramCrawlingStrategy instagramCrawlingStrategy;

	/**
	 * 크롤링 비동기 처리
	 */
	@Async("crawlingTaskExecutor")
	public CompletableFuture<CrawlCompletionResponse> crawlAsync(String url) {
		try {
			log.debug("[Crawl] 크롤링 작업 시작: {}", url);  // 전체 작업 시작 로그
			WebCrawlingStrategy strategy = findStrategy(url);
			CrawlCompletionResponse contents;

			if (url.contains(INSTAGRAM_URL)) {
				contents = strategy.extractContentsUsingApify(url);
				// Instagram 관련 로그는 InstagramCrawlingStrategy에서 처리하므로 여기서는 제거
			} else {
				Document document = strategy.getDocument(url);
				contents = strategy.extractContents(document);
			}
			return CompletableFuture.completedFuture(contents);
		} catch (CrawlingException e) {
			log.error("[Crawl] 크롤링 실패: {}, 원인: {}", url, e.getMessage());
			return CompletableFuture.failedFuture(e);
		} catch (Exception e) {
			log.error("[Crawl] 예상치 못한 오류: {}, 원인: {}", url, e.getMessage());
			return CompletableFuture.failedFuture(new CrawlingException(GlobalErrorCode.CRAWLING_FAILED));
		}
	}

	/**
	 * 플랫폼 구별
	 */
	private WebCrawlingStrategy findStrategy(String url) {
		if (url == null || url.isEmpty()) {
			log.error("[Crawl] URL이 비어있음");
			throw new CrawlingException(GlobalErrorCode.INVALID_URL);
		}

		return crawlingStrategies.stream()
			.filter(strategy -> strategy.supports(url))
			.findFirst()
			.orElseThrow(() -> {
				log.error("[Crawl] 지원하지 않는 플랫폼: {}", url);
				return new CrawlingException(GlobalErrorCode.PLATFORM_NOT_SUPPORTED);
			});
	}
}