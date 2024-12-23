package com.adit.backend.domain.auth.dto.request;

import org.springframework.web.bind.annotation.RequestParam;

public record KakaoRequest() {

	public record AuthDto(@RequestParam("code") String code) {
	}

	public record AccessTokenDto(String accessToken) {
	}

	public record RefreshTokenDto(String refreshToken) {
	}

}
