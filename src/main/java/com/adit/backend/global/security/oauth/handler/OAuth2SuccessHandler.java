package com.adit.backend.global.security.oauth.handler;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.auth.dto.response.LoginResponse;
import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.global.common.ApiResponse;
import com.adit.backend.global.security.jwt.entity.RefreshToken;
import com.adit.backend.global.security.jwt.entity.Token;
import com.adit.backend.global.security.jwt.repository.RefreshTokenRepository;
import com.adit.backend.global.security.jwt.util.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final ObjectMapper objectMapper;
	@Value("${token.refresh.expiration}")
	private Long refreshTokenExpirationAt;

	@Value("${token.access.header}")
	private String accessTokenHeader;

	@Value("${token.refresh.cookie.name}")
	private String refreshTokenCookieName;

	private final JwtTokenProvider tokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		PrincipalDetails userDetails = tokenProvider.getUserDetails(authentication);

		//Access Token 생성 및 응답 헤더 추가
		Token token = tokenProvider.createToken(authentication);
		response.addHeader(accessTokenHeader, "Bearer " + token.getAccessToken());

		//Refresh Token 생성 및 응답 쿠키 추가
		RefreshToken refreshToken = new RefreshToken(userDetails.getUser().getId(), token.getRefreshToken());
		refreshToken.updateRefreshToken(token.getRefreshToken());
		refreshTokenRepository.save(refreshToken);

		Cookie cookie = new Cookie(refreshTokenCookieName, token.getRefreshToken());
		cookie.setPath("/");
		ZonedDateTime seoulTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		ZonedDateTime expirationTime = seoulTime.plusSeconds(refreshTokenExpirationAt);
		cookie.setMaxAge((int)(expirationTime.toEpochSecond() - seoulTime.toEpochSecond()));
		//cookie.setSecure(true);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);

		// Role 정보를 응답 본문에 추가
		LoginResponse loginResponse = LoginResponse.from(userDetails.getUser().getRole());
		ApiResponse<LoginResponse> apiResponse = ApiResponse.success(loginResponse);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

		log.info("[Token] JWT 토큰 생성 및 발급 email : {}", userDetails.getUsername());
	}
}