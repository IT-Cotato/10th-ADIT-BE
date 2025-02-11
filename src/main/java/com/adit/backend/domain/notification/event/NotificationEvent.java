package com.adit.backend.domain.notification.event;

import com.adit.backend.domain.notification.enums.NotificationType;

import lombok.Builder;

@Builder
public record NotificationEvent(
	String userKey,
	String message,
	NotificationType notificationType,
	String relatedUri
) {}
