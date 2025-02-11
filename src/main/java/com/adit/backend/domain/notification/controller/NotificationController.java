package com.adit.backend.domain.notification.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.adit.backend.domain.notification.dto.NotificationResponse;
import com.adit.backend.domain.notification.service.command.NotificationCommandService;
import com.adit.backend.domain.notification.service.query.NotificationQueryService;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.common.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationController {

	private final NotificationCommandService notificationCommandService;
	private final NotificationQueryService notificationQueryService;

	// 응답 시 MIME 타입을 text/event-stream으로 보내야한다.
	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal(expression = "user") User user) {
		return ResponseEntity.ok(notificationCommandService.subscribe(user.getEmail()));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> notifications(@AuthenticationPrincipal(expression = "user") User user) {
		return ResponseEntity.ok(ApiResponse.success(notificationQueryService.getAllNotifications(user.getEmail())));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Void> redirect(@PathVariable Long id) {
		URI redirectUri = notificationCommandService.getRedirectUri(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(redirectUri);
		return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
	}
}