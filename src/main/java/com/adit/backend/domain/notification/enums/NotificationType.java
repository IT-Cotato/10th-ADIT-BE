package com.adit.backend.domain.notification.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum NotificationType {

	/* ANNOUNCEMENT */
	ANNOUNCEMENT("공지사항 알림", "ANNOUNCEMENT"),

	/* PLACE */
	PLACE_UNVISITED("미 방문 장소 알림", "PLACE"),

	/* EVENT */
	EVENT_START_SOON("이벤트 시작일 임박 알림", "EVENT"),
	EVENT_DURATION_MISSING("이벤트 기간 누락 알림", "EVENT"),

	/* FRIEND */
	FRIEND_REQUEST_RECEIVED("친구 요청 수신 알림", "FRIEND"),
	FRIEND_REQUEST_ACCEPTED("친구 요청 수락 알림", "FRIEND"),
	FRIEND_SAVED_MY_PLACE("친구가 같은 장소 저장 알림", "FRIEND");

	private final String description;
	private final String category;
}
