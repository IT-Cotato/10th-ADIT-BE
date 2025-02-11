package com.adit.backend.domain.notification.service;

import static com.adit.backend.domain.notification.constants.Channel.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.adit.backend.domain.notification.dto.NotificationResponse;
import com.adit.backend.domain.notification.service.handler.RedisSubscriber;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RedisMessageService {

	private final RedisMessageListenerContainer container;
	private final RedisSubscriber subscriber;
	private final RedisTemplate<String, Object> redisTemplate;

	// 채널 구독
	public void subscribe(String channel) {
		container.addMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
	}

	// 이벤트 발행
	public void publish(String channel, NotificationResponse response) {
		redisTemplate.convertAndSend(getChannelName(channel), response);
	}

	// 구독 삭제
	public void removeSubscribe(String channel) {
		container.removeMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
	}

	private String getChannelName(String id) {
		return CHANNEL_PREFIX + id;
	}
}
