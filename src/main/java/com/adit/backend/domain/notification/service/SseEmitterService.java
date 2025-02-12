package com.adit.backend.domain.notification.service;

import java.io.IOException;

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

	private static final Long TIMEOUT = 30 * 60 * 1000L;

	public SseEmitter createEmitter(String userEmail) {
		return sseEmitterRepository.save(userEmail, new SseEmitter(TIMEOUT));
	}

	public void deleteEmitter(String userEmail) {
		sseEmitterRepository.deleteByUserEmail(userEmail);
	}

	public void sendNotificationToClient(String userEmail, NotificationResponse response) {
		sseEmitterRepository.findById(userEmail)
			.ifPresent(emitter -> send(response, userEmail, emitter));
	}

	public void send(Object data, String userEmail, SseEmitter sseEmitter) {
		try {
			log.info("send to client {}:[{}]", userEmail, data);
			sseEmitter.send(SseEmitter.event()
				.id(userEmail)
				.data(data, MediaType.APPLICATION_JSON));
		} catch (IOException | IllegalStateException e) {
			log.error("[SSE] IOException 또는 IllegalStateException이 발생했습니다.", e);
			sseEmitterRepository.deleteByUserEmail(userEmail);
		}
	}
}