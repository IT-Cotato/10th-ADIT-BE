package com.adit.backend.domain.ai.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.concurrent.CompletableFuture;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.adit.backend.domain.ai.dto.response.ContentListResponse;
import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.domain.ai.exception.AiException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 요약 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAiService {

	private final ChatClient chatClient;
	private final ContentService contentService;
	@Value("classpath:/prompts/culture-info-prompt.st")
	private Resource prompt;
	@Value("classpath:/prompts/culture-info-system.st")
	private Resource system;

	/**
	 * URL에서 데이터를 추출 및 요약
	 */
	public ContentListResponse summaryContent(final String url) {
		return contentService.extractContents(url)
			.thenCompose(extractedContent -> {
				log.debug("[AI] 웹페이지 크롤링 완료 - URL: {}", url);
				log.trace("[AI] 추출된 컨텐츠: {}", extractedContent);
				return processWithAI(extractedContent);
			})
			.exceptionally(throwable -> {
				log.error("[AI] 토큰 사용량 초과 - URL: {}", url);
				throw new AiException(EXCEEDING_TOKEN_USAGE);
			}).join();
	}

	/**
	 *  AI 요약
	 */
	private CompletableFuture<ContentListResponse> processWithAI(CrawlCompletionResponse extractedContent) {
		BeanOutputConverter<ContentListResponse> converter = new BeanOutputConverter<>(ContentListResponse.class);
		PromptTemplate promptTemplate = generatePromptTemplate(extractedContent);
		return CompletableFuture.supplyAsync(() -> {
			try {
				log.debug("[AI] AI 처리 시작");
				String response = chatClient.prompt()
					.system(system)
					.user(promptTemplate.render() + converter.getFormat())
					.call()
					.content();
				log.info("[AI] AI 요약 완료");
				log.debug("[AI] AI 응답: {}", response);
				return ContentListResponse.builder()
					.contentResponseList(converter.convert(response).contentResponseList())
					.imageSrcList(extractedContent.imageSrcList())
					.build();
			} catch (RuntimeException exception) {
				log.error("[AI] AI 처리 실패 - 원인: {}", exception.getMessage());
				throw new AiException(AI_PROCESSING_FAILED);
			}
		});
	}

	/**
	 *  프롬프트 정의
	 */
	private PromptTemplate generatePromptTemplate(final CrawlCompletionResponse extractedContent) {
		try {
			PromptTemplate promptTemplate = new PromptTemplate(prompt);
			promptTemplate.add("extractedContent", extractedContent.crawlingData());
			log.trace("[AI] 프롬프트 생성 완료: {}", promptTemplate);
			return promptTemplate;
		} catch (Exception e) {
			log.error("[AI] 프롬프트 생성 실패: {}", e.getMessage());
			throw new AiException(AI_RESPONSE_FAILED);
		}
	}
}

