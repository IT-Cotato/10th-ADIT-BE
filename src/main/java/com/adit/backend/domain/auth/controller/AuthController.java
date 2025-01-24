package com.adit.backend.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.auth.dto.response.ReissueResponse;
import com.adit.backend.domain.auth.service.AuthService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "Auth API", description = "사용자 토큰 발급 관련 API")
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "사용자 토큰 재발급", description = "사용자의 RefreshToken을 이용하여 JWT 토큰을 재 발급합니다.")
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<ReissueResponse>> tokenReissue(@CookieValue(name = "refreshToken") String refreshToken,
		HttpServletResponse response) {
		return ResponseEntity.ok(ApiResponse.success(authService.reIssue(refreshToken, response)));
	}

	@Operation(summary = "사용자 로그아웃", description = "사용자의 JWT 토큰을 제거하고 로그아웃합니다.")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken") String refreshToken,
		HttpServletResponse response) {
		authService.logout(refreshToken, response);
		return ResponseEntity.noContent().build();
	}

}