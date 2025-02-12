package com.adit.backend.domain.notification.converter;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.notification.dto.NotificationResponse;
import com.adit.backend.domain.notification.entity.Notification;
import com.adit.backend.domain.notification.enums.NotificationType;

@Component
public class NotificationConverter {

	public Notification toEntity(String message, NotificationType notificationType) {
		return Notification.builder()
			.message(message)
			.notificationType(notificationType)
			.category(notificationType.getCategory())
			.build();
	}

	public NotificationResponse toResponse(Notification notification) {
		return NotificationResponse
			.builder()
			.message(notification.getMessage())
			.notificationType(notification.getNotificationType())
			.category(notification.getNotificationType().getCategory())
			.isRead(notification.isRead())
			.createdAt(notification.getCreatedAt())
			.build();
	}
}
