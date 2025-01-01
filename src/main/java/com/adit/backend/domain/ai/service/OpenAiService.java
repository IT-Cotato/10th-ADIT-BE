package com.adit.backend.domain.ai.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.adit.backend.domain.ai.dto.response.ContentResponse;
import com.adit.backend.domain.ai.dto.response.CrawlCompletionResponse;
import com.adit.backend.domain.ai.exception.AiException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

	public CompletableFuture<List<ContentResponse>> analyzeCulturalInfo(final String url) {
		return contentService.extractContents(url)
			.thenCompose(extractedContent -> {
				log.info("Extracted content successfully: {}", extractedContent);
				return processWithAI(extractedContent);
			})
			.exceptionally(throwable -> {
				log.error("Error during cultural info analysis", throwable);
				throw new AiException(FAIL_CONVERT_RESPONSE);
			});
	}

	private CompletableFuture<List<ContentResponse>> processWithAI(CrawlCompletionResponse extractedContent) {
		BeanOutputConverter<List<ContentResponse>> converter = new BeanOutputConverter<>(
			(Class<List<ContentResponse>>)(Class<?>)List.class
		);
		PromptTemplate promptTemplate = generatePromptTemplate(extractedContent);
		promptTemplate.add("extractedContent", extractedContent.crawlingData());

		return CompletableFuture.supplyAsync(() -> {
			try {
				String response = chatClient.prompt()
					.system(system)
					.user(promptTemplate.render() + converter.getFormat())
					.call()
					.content();

				log.info("AI response received: {}", response);
				return converter.convert(response);
			} catch (RuntimeException exception) {
				log.error("Error during AI response conversion: {}", exception.getMessage());
				throw new AiException(FAIL_CONVERT_RESPONSE);
			}
		});
	}

	private PromptTemplate generatePromptTemplate(final CrawlCompletionResponse extractedContent) {
		PromptTemplate promptTemplate = new PromptTemplate(prompt);
		promptTemplate.add("extractedContent", extractedContent.crawlingData());
		return promptTemplate;
	}
}
