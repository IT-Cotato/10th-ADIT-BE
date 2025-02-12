package com.adit.backend.domain.notification.controller;

	import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.adit.backend.domain.notification.converter.NotificationConverter;
import com.adit.backend.domain.notification.dto.NotificationResponse;
import com.adit.backend.domain.notification.enums.NotificationType;
import com.adit.backend.domain.notification.event.NotificationEvent;
import com.adit.backend.domain.notification.service.command.NotificationCommandService;
import com.adit.backend.domain.notification.service.query.NotificationQueryService;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationController {

	private final NotificationCommandService notificationCommandService;
	private final NotificationQueryService notificationQueryService;
	private final NotificationConverter notificationConverter;

	@Operation(summary = "알람 sse 구독 및 누락 알람 수신", description = "알람 sse 구독, 마지막 이벤트 ID를 기반으로 누락된 알람 수신")
	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal(expression = "user") User user,
		@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
		return ResponseEntity.ok(notificationCommandService.subscribe(user.getEmail(), lastEventId));
	}

	@Operation(summary = "알람 리스트 수신", description = "최근 일주일 내의 모은 알람 내역을 반환")
	@GetMapping
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> notifications(
		@AuthenticationPrincipal(expression = "user") User user) {
		return ResponseEntity.ok(ApiResponse.success(notificationQueryService.getRecentNotifications(user.getEmail())
			.stream()
			.map(notificationConverter::toResponse)
			.toList()));
	}

	@PostMapping("/test")
	public ResponseEntity<ApiResponse<String>> sendTestNotification(
		@AuthenticationPrincipal(expression = "user") User user) {
		// 테스트용 더미 알림 이벤트 생성
		NotificationEvent testEvent = NotificationEvent.builder()
			.userEmail(user.getEmail())
			.message("Test notification from server.")
			.notificationType(NotificationType.FRIEND_SAVED_MY_PLACE)  // 테스트용 알림 타입 선택
			.build();

		// 알림 발송 (내부에서 DB 저장 및 Redis를 통한 SSE 브로드캐스트 수행)
		notificationCommandService.sendNotification(testEvent);

		return ResponseEntity.ok(ApiResponse.success("Test notification sent successfully."));
	}
}