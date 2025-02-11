package com.adit.backend.domain.notification.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.notification.service.command.NotificationCommandService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NotificationEventHandler {

	private final NotificationCommandService notificationCommandService;

	@Async
	@EventListener // 이벤트 구독
	public void handleEvent(NotificationEvent event) {
		notificationCommandService.sendNotification(event); // 알림 발송 메서드
	}
}