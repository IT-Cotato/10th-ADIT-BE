package com.adit.backend.domain.notification.event;

import java.time.LocalDateTime;

import com.adit.backend.domain.notification.enums.NotificationType;

import lombok.Builder;

@Builder
public record NotificationEvent(
	String userEmail,
	String message,
	String category,
	NotificationType notificationType,
	LocalDateTime createdAt
) {
}
