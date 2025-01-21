package com.adit.backend.global.security.oauth.handler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.global.security.jwt.service.JwtTokenService;
import com.adit.backend.global.security.jwt.util.JwtTokenProvider;

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

	@Value("${token.refresh.expiration}")
	private Long refreshTokenExpirationAt;

	@Value("${token.access.header}")
	private String accessTokenHeader;

	@Value("${token.refresh.cookie.name}")
	private String refreshTokenCookieName;

	private final JwtTokenProvider tokenProvider;
	private final JwtTokenService jwtTokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {
		PrincipalDetails userDetails = getUserDetails(authentication);
		String accessToken = tokenProvider.generateAccessToken(authentication);
		String refreshToken = tokenProvider.generateRefreshToken(authentication);
		jwtTokenService.saveOrUpdate(userDetails.getUsername(), refreshToken, accessToken);

		response.setHeader(accessTokenHeader, accessToken);
		Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
		cookie.setPath("/");
		ZonedDateTime seoulTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		ZonedDateTime expirationTime = seoulTime.plusSeconds(refreshTokenExpirationAt);
		cookie.setMaxAge((int)(expirationTime.toEpochSecond() - seoulTime.toEpochSecond()));
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		log.info("로그인에 성공하였습니다. email | AccessToken | RefreshToken: {} | {} | {}",
			userDetails.getUsername(), accessToken, refreshToken);

	}

	private PrincipalDetails getUserDetails(Authentication authentication) {
		return (PrincipalDetails)authentication.getPrincipal();
	}

}