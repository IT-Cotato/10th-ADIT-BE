package com.adit.backend.domain.notification.dto;

import java.time.LocalDateTime;

import com.adit.backend.domain.notification.enums.NotificationType;

import lombok.Builder;

@Builder
public record NotificationResponse(String message,
								   String category,
								   NotificationType notificationType,
								   LocalDateTime createdAt,
								   boolean isRead) {
}

