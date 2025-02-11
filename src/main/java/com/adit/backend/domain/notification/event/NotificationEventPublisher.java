package com.adit.backend.domain.notification.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.notification.entity.Notification;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishEvent(Notification event) {
		eventPublisher.publishEvent(event);
	}
}
