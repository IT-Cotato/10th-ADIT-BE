package com.adit.backend.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.auth.annotation.LogoutResponse;
import com.adit.backend.domain.auth.annotation.OAuthLoginResponse;
import com.adit.backend.domain.auth.annotation.RefreshTokenCookie;
import com.adit.backend.domain.auth.annotation.ReissueTokenResponse;
import com.adit.backend.domain.auth.dto.request.KakaoRequest;
import com.adit.backend.domain.auth.dto.response.LoginResponse;
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

	@OAuthLoginResponse
	@GetMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> oauthLogin(KakaoRequest.AuthDto code,
		HttpServletResponse response) {
		return ResponseEntity.ok(ApiResponse.success(authService.login(code, response)));
	}

	@ReissueTokenResponse
	@Operation(summary = "사용자 토큰 재발급", description = "사용자의 RefreshToken을 이용하여 JWT 토큰을 재 발급합니다.")
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<ReissueResponse>> tokenReissue(
		@RefreshTokenCookie @CookieValue(name = "refreshToken") String refreshToken,
		HttpServletResponse response) {
		return ResponseEntity.ok(ApiResponse.success(authService.reIssue(refreshToken, response)));
	}

	@LogoutResponse
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
		@RefreshTokenCookie @CookieValue(name = "refreshToken") String refreshToken,
		HttpServletResponse response) {
		authService.logout(refreshToken, response);
		return ResponseEntity.noContent().build();
	}
}