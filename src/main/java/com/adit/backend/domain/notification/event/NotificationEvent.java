package com.adit.backend.domain.notification.event;

import com.adit.backend.domain.notification.enums.NotificationType;

import lombok.Builder;

@Builder
public record NotificationEvent(
	String userEmail,
	String message,
	NotificationType notificationType) {}
