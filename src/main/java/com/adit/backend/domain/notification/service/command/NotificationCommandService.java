package com.adit.backend.domain.notification.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.adit.backend.domain.notification.constants.MsgFormat;
import com.adit.backend.domain.notification.converter.NotificationConverter;
import com.adit.backend.domain.notification.entity.Notification;
import com.adit.backend.domain.notification.event.NotificationEvent;
import com.adit.backend.domain.notification.repository.NotificationRepository;
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

	private final UserQueryService userQueryService;
	private final SseEmitterService sseEmitterService;
	private final RedisMessageService redisMessageService;
	private final NotificationQueryService notificationQueryService;

	private final NotificationConverter converter;

	/**
	 * 사용자가 SSE 구독을 시작합니다.
	 *
	 * @param userEmail   사용자 이메일
	 * @param lastEventId 마지막 이벤트 ID
	 * @return 생성된 SSE Emitter 객체
	 */
	public SseEmitter subscribe(String userEmail, String lastEventId) {
		SseEmitter sseEmitter = sseEmitterService.createEmitter(userEmail);
		sseEmitterService.send(MsgFormat.SUBSCRIBE, userEmail, sseEmitter);
		redisMessageService.subscribe(userEmail);

		sseEmitter.onCompletion(() -> {
			log.debug("[SSE] onCompletion callback : {}", userEmail);
			sseEmitterService.deleteEmitter(userEmail);
			redisMessageService.removeSubscribe(userEmail);
		});
		sseEmitter.onTimeout(() -> {
			log.warn("[SSE] Timeout callback: {}", userEmail);
			sseEmitter.complete();
		});
		sseEmitter.onError((e) -> {
			log.error("[SSE] 에러 발생", e);
			sseEmitter.complete();
		});

		notificationQueryService.sendMissedNotifications(lastEventId, userEmail, sseEmitter);

		return sseEmitter;
	}

	/**
	 * 주어진 이벤트를 기반으로 알림을 생성하고 저장합니다.
	 * 알림을 생성한 후, Redis를 통해 해당 사용자에게 알림을 발행합니다.
	 *
	 * @param event 알림 이벤트 객체
	 */
	@Transactional
	public void sendNotification(NotificationEvent event) {
		User user = userQueryService.findUserByEmail(event.userEmail());

		Notification notification = converter.toEntity(event.message(), event.notificationType());
		notification.assignUser(user);
		notificationRepository.save(notification);

		redisMessageService.publish(event.userEmail(), converter.toResponse(notification));
	}
}
