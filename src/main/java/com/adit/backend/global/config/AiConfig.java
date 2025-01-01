package com.adit.backend.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

	/**
	 * Chat Client dependency injection.
	 *
	 * @return the chat client
	 */
	@Bean
	public ChatClient chatClient() {
		ChatModel chatModel = new OpenAiChatModel(new OpenAiApi(apiKey));
		return ChatClient
			.builder(chatModel)
			.build();
	}

}