package com.adit.backend.domain.notification.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.adit.backend.domain.notification.dto.NotificationResponse;
import com.adit.backend.domain.notification.repository.SseEmitterRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SseEmitterService {

	private final SseEmitterRepository sseEmitterRepository;

	@Value("${sse.timeout}")
	private Long timeout;

	public SseEmitter createEmitter(String emitterKey) {
		return sseEmitterRepository.save(emitterKey, new SseEmitter(timeout));
	}

	public void deleteEmitter(String emitterKey) {
		sseEmitterRepository.deleteById(emitterKey);
	}

	public void sendNotificationToClient(String emitterKey, NotificationResponse response) {
		sseEmitterRepository.findById(emitterKey)
			.ifPresent(emitter -> send(response, emitterKey, emitter));
	}

	public void send(Object data, String emitterKey, SseEmitter sseEmitter) {
		try {
			log.info("send to client {}:[{}]", emitterKey, data);
			sseEmitter.send(SseEmitter.event()
				.id(emitterKey)
				.data(data, MediaType.APPLICATION_JSON));
		} catch (IOException | IllegalStateException e) {
			log.error("[Notification] IOException 또는 IllegalStateException이 발생했습니다.", e);
			sseEmitterRepository.deleteById(emitterKey);
		}
	}
}
