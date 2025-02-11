package com.adit.backend.domain.notification.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.notification.converter.NotificationConverter;
import com.adit.backend.domain.notification.dto.NotificationResponse;
import com.adit.backend.domain.notification.entity.Notification;
import com.adit.backend.domain.notification.exception.NotificationException;
import com.adit.backend.domain.notification.repository.NotificationRepository;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationQueryService {

	private final NotificationRepository notificationRepository;
	private final UserQueryService userQueryService;
	private final NotificationConverter converter;

	public Notification findNotificationById(Long id) {
		return notificationRepository.findById(id)
			.orElseThrow(() -> new NotificationException(NOTIFICATION_NOT_FOUND));
	}

	public List<NotificationResponse> getAllNotifications(String userKey) {
		User user = userQueryService.findUserByEmail(userKey);
		return notificationRepository.findAllByUser(user)
			.stream().map(converter::toResponse)
			.toList();
	}
}
