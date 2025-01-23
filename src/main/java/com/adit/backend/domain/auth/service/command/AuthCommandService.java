package com.adit.backend.domain.auth.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.response.ReissueResponse;
import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.auth.service.query.TokenQueryService;
import com.adit.backend.global.security.jwt.exception.AuthException;
import com.adit.backend.global.security.jwt.util.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthCommandService {
	private final TokenQueryService tokenQueryService;
	private final TokenCommandService tokenCommandService;
	private final JwtTokenProvider jwtTokenProvider;

	@Value("${token.refresh.expiration}")
	private String refreshTokenExpiresAt;

	@Value("${token.refresh.cookie.name}")
	private String refreshTokenCookieName;

	public ReissueResponse reIssue(String refreshToken, HttpServletResponse response) {
		Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
		Token token = tokenQueryService.validateRefreshToken(refreshToken);
		log.info("[브라우저에서 들어온 쿠키] == [DB에 저장된 토큰], {}", refreshToken.equals(token.getRefreshToken()));
		if (!refreshToken.equals(token.getRefreshToken())) {
			log.warn("[쿠키로 들어온 토큰과 DB의 토큰이 일치하지 않음.]");
			throw new AuthException(TOKEN_NOT_FOUND);
		}
		jwtTokenProvider.checkRefreshTokenAndReIssueAccessToken(authentication, refreshToken);
		addRefreshTokenToCookie(null, response);
		return ReissueResponse.from(token.getAccessToken());
	}

	@Transactional
	public void logout(String refreshToken, HttpServletResponse response) {
		Token existToken = tokenQueryService.validateRefreshToken(refreshToken);
		tokenCommandService.deleteToken(existToken);
		addRefreshTokenToCookie(null, response);
		log.info("[로그아웃 진행 완료]");
	}

	private void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
		Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
		cookie.setPath("/");
		ZonedDateTime seoulTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		ZonedDateTime expirationTime = seoulTime.plusSeconds(Long.parseLong(refreshTokenExpiresAt));
		cookie.setMaxAge((int)(expirationTime.toEpochSecond() - seoulTime.toEpochSecond()));
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		log.info("[쿠키 생성 완료] Cookie: {}", cookie.getValue());
	}

}

