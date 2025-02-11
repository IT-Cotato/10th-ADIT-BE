package com.adit.backend.domain.notification.service.handler;

import static com.adit.backend.domain.notification.constants.Channel.*;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.notification.dto.NotificationResponse;
import com.adit.backend.domain.notification.service.SseEmitterService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RedisSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final SseEmitterService sseEmitterService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String channel = new String(message.getChannel())
				.substring(CHANNEL_PREFIX.length());

			NotificationResponse response = objectMapper.readValue(message.getBody(),
				NotificationResponse.class);

			// 클라이언트에게 event 데이터 전송
			sseEmitterService.sendNotificationToClient(channel, response);
		} catch (IOException e) {
			log.error("IOException is occurred. ", e);
		}
	}
}
