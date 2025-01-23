package com.adit.backend.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.auth.dto.response.ReissueResponse;
import com.adit.backend.domain.auth.service.command.AuthCommandService;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthController {

	private final AuthCommandService authCommandService;
/*

	@Operation(summary = "카카오 회원가입 응답", description = "")
	@Parameter(name = "code", description = "카카오 인가 코드", required = true)
	@GetMapping("/join")
	public ResponseEntity<String> joinAuth(KakaoRequest.AuthDto request,
		HttpServletResponse response) {
		return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
	}
*/

	@Operation(summary = "사용자 토큰 재발급", description = "사용자의 JWT 토큰을 재 발급합니다.")
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<ReissueResponse>> tokenReissue(
		@CookieValue(name = "refreshToken") String refreshToken,
		HttpServletResponse response) {
		return ResponseEntity.ok(ApiResponse.success(authCommandService.reIssue(refreshToken, response)));
	}

	@Operation(summary = "사용자 로그아웃", description = "사용자의 JWT 토큰을 제거하고 로그아웃합니다.")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken") String refreshToken,
		HttpServletResponse response) {
		authCommandService.logout(refreshToken, response);
		return ResponseEntity.noContent().build();
	}

}