package com.adit.backend.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adit.backend.domain.ai.util.LoggingAdvisor;

@Configuration
public class AiConfig {

	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

	@Value("${spring.ai.openai.chat.options.model}")
	private String defaultModel;

	@Value("${spring.ai.openai.chat.options.max-tokens}")
	private int maxCompletionToken;

	@Bean
	ChatMemory chatMemory() {
		return new InMemoryChatMemory();
	}

	/**
	 * Chat Client dependency injection.
	 *
	 * @return the chat client
	 */
	@Bean
	public ChatClient chatClient() {
		ChatModel chatModel = new OpenAiChatModel(new OpenAiApi(apiKey),
			OpenAiChatOptions.builder()
				.model(defaultModel)
				.maxCompletionTokens(maxCompletionToken)
				.build());

		return ChatClient
			.builder(chatModel)
			.defaultAdvisors(
				new MessageChatMemoryAdvisor(chatMemory(),
					AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 10),
				new LoggingAdvisor()
			)
			.build();
	}

}