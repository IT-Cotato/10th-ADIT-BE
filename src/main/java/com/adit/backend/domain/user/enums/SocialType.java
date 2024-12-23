package com.adit.backend.domain.user.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SocialType {
	KAKAO("카카오"),
	GOOGLE("구글"),
	NAVER("네이버");

	private final String description;
}