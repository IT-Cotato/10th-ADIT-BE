package com.adit.backend.domain.notification.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

	public List<NotificationResponse> getRecentNotifications(String userEmail) {
		User user = userQueryService.findUserByEmail(userEmail);
		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
		return notificationRepository.findRecentNotifications(user, cutoffDate)
			.orElse(Collections.emptyList())
			.stream().map(converter::toResponse)
			.toList();
	}

	public List<Notification> getMissedNotifications(User user, Long lastId) {
		return notificationRepository.findAllByUserAndIdGreaterThan(user, lastId)
			.orElse(Collections.emptyList());
	}

	/**
	 * 주어진 사용자 이메일과 마지막 이벤트 ID를 기반으로 누락된 알림을 찾아 SSE로 전송합니다.
	 *
	 * @param lastEventId 마지막 이벤트 ID (클라이언트가 재접속 시 전달)
	 * @param userEmail   사용자 이메일
	 * @param emitter     SSE Emitter
	 */
	public void sendMissedNotifications(String lastEventId, String userEmail, SseEmitter emitter) {
		if (lastEventId.isEmpty()) {
			return;
		}

		Long lastId = Long.parseLong(lastEventId);
		User user = userQueryService.findUserByEmail(userEmail);
		List<Notification> missedNotifications = getMissedNotifications(user, lastId);

		missedNotifications.stream()
			.sorted(Comparator.comparingLong(Notification::getId))
			.forEach(notification -> {
				try {
					emitter.send(SseEmitter.event()
						.id(String.valueOf(notification.getId()))
						.data(converter.toResponse(notification)));
				} catch (IOException e) {
					log.error("[SSE] 누락된 알림 수신 중 오류 발생", e);
					emitter.completeWithError(e);
				}
			});
	}

	public List<Notification> getNotificationsByCategory(User user, String category) {
		return notificationRepository.findByUserAndCategory(user, category)
			.orElse(Collections.emptyList());
	}
}
