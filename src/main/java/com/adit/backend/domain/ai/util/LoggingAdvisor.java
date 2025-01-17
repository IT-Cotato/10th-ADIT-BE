package com.adit.backend.domain.ai.util;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class LoggingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	@Override
	public String getName() {
		return "LoggingAdvisor";
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		log.debug("REQUEST");
		log.debug(String.valueOf(advisedRequest));
		AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
		log.debug("RESPONSE");
		log.debug(String.valueOf(advisedResponse));
		log.info("[Input Token Usage] : {}", advisedResponse.response().getMetadata().getUsage().getPromptTokens());
		log.info("[Output Token Usage] : {}", advisedResponse.response().getMetadata().getUsage().getGenerationTokens());
		log.info("[Total Token Usage] : {}", advisedResponse.response().getMetadata().getUsage().getTotalTokens());
		return advisedResponse;

	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		System.out.println("\nRequest: " + advisedRequest);
		Flux<AdvisedResponse> responses = chain.nextAroundStream(advisedRequest);
		return new MessageAggregator().aggregateAdvisedResponse(responses, aggregatedAdvisedResponse -> {
			System.out.println("\nResponse: " + aggregatedAdvisedResponse);
		});
	}

}