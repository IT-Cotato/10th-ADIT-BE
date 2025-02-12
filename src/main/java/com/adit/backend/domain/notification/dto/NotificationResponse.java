package com.adit.backend.domain.notification.dto;

import com.adit.backend.domain.notification.enums.NotificationType;

import lombok.Builder;

@Builder
public record NotificationResponse(String message, NotificationType notificationType, boolean isRead) {
}

