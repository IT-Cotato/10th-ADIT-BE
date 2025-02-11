package com.adit.backend.domain.notification.service.command;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.adit.backend.domain.notification.constants.MsgFormat;
import com.adit.backend.domain.notification.converter.NotificationConverter;
import com.adit.backend.domain.notification.entity.Notification;
import com.adit.backend.domain.notification.event.NotificationEvent;
import com.adit.backend.domain.notification.repository.NotificationRepository;
import com.adit.backend.domain.notification.repository.SseEmitterRepository;
import com.adit.backend.domain.notification.service.RedisMessageService;
import com.adit.backend.domain.notification.service.SseEmitterService;
import com.adit.backend.domain.notification.service.query.NotificationQueryService;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationCommandService {
	private final NotificationRepository notificationRepository;
	private final SseEmitterRepository sseEmitterRepository;
	private final UserQueryService userQueryService;
	private final NotificationConverter converter;
	private final SseEmitterService sseEmitterService;
	private final RedisMessageService redisMessageService;
	private final NotificationQueryService notificationQueryService;

	@Value("${sse.timeout}")
	private Long timeout;

	public SseEmitter subscribe(String userKey) {
		SseEmitter sseEmitter = sseEmitterService.createEmitter(userKey);
		sseEmitterService.send(MsgFormat.SUBSCRIBE, userKey, sseEmitter); // send dummy

		redisMessageService.subscribe(userKey);

		sseEmitter.onTimeout(sseEmitter::complete);
		sseEmitter.onError((e) -> sseEmitter.complete());
		sseEmitter.onCompletion(() -> {
			sseEmitterService.deleteEmitter(userKey);
			redisMessageService.removeSubscribe(userKey);
		});
		return sseEmitter;
	}

	@Transactional
	public void sendNotification(NotificationEvent event) {
		User user = userQueryService.findUserByEmail(event.userKey());
		Notification notification = converter.toEntity(event.message(), event.notificationType(), event.relatedUri());
		notification.assignUser(user);
		notificationRepository.save(notification);
		redisMessageService.publish(event.userKey(), converter.toResponse(notification));
	}

	@Transactional
	public URI getRedirectUri(Long notificationId) {
		Notification notification = notificationQueryService.findNotificationById(notificationId);
		notification.markAsRead();
		return URI.create(notification.getRelatedUri());
	}

}
