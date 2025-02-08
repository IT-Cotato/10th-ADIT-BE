package com.adit.backend.global.security.jwt.filter;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.adit.backend.global.security.jwt.enums.TokenStatus;
import com.adit.backend.global.security.jwt.exception.TokenException;
import com.adit.backend.global.security.jwt.util.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private static final String AUTH_PATH = "/api/auth/**";
	private static final String LOGIN_PATH = "/login";
	private static final String[] WHITE_LIST = {
		"/",
		"/oauth2/**",
		"/login/**",
		"/kakao/**",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/swagger-resources/**",
		"/v3/api-docs/**",
		"/webjars/**",
		"/favicon.ico"
	};

	private final JwtTokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		// white list 또는 인증/로그인 관련 경로면 토큰 검증을 건너뜁니다.
		if (shouldNotFilter(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		String accessToken = tokenProvider.extractAccessTokenFromHeader(request);
		log.debug("[Token] JwtAuthorizationFilter 토큰 검증 accessToken : {}", accessToken);
		TokenStatus tokenStatus = tokenProvider.validateAccessToken(accessToken);
		if (tokenStatus == TokenStatus.VALID) {
			setAuthentication(accessToken);
		} else if (tokenStatus == TokenStatus.EXPIRED) {
			throw new TokenException(ACCESS_TOKEN_EXPIRED);
		} else {
			throw new TokenException(INVALID_TOKEN);
		}

		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String accessToken) {
		try {
			Authentication authentication = tokenProvider.getAuthentication(accessToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (Exception e) {
			SecurityContextHolder.clearContext();
			log.error("[Authentication] 사용자 인증 설정 실패", e);
			throw new TokenException(INVALID_TOKEN);
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		log.trace("[Request] 요청 경로, 메서드: {} {}", uri, request.getMethod());
		return isAuthPath(uri) || isWhiteList(request);
	}

	// HTTP 메서드에 상관없이 URL 패턴만으로 화이트리스트 적용
	private boolean isWhiteList(HttpServletRequest request) {
		AntPathMatcher matcher = new AntPathMatcher();
		return Arrays.stream(WHITE_LIST)
			.anyMatch(pattern -> matcher.match(pattern, request.getRequestURI()));
	}

	private boolean isAuthPath(String requestURI) {
		AntPathMatcher matcher = new AntPathMatcher();
		return matcher.match(AUTH_PATH, requestURI) || matcher.match(LOGIN_PATH, requestURI);
	}
}
