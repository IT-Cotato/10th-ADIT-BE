package com.adit.backend.global.security.jwt.filter;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.TokenException;
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
		"/api/ai/**",
		"/api/user/**",
		"/api/scraper/**",
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
		String accessToken = tokenProvider.extractAccessTokenFromHeader(request).get();
		tokenProvider.isAccessTokenValid(accessToken);
		setAuthentication(accessToken);
		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String accessToken) {
		try {
			Authentication authentication = tokenProvider.getAuthentication(accessToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (Exception e) {
			SecurityContextHolder.clearContext();
			log.error("[Authentication] 사용자 인증 설정 실패", e.getCause());
			throw new TokenException(GlobalErrorCode.INVALID_TOKEN);
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		log.info("[Log] 요청 경로, 메서드: {}, {}", path, request.getMethod());
		return isAuthPath(request.getRequestURI()) || isWhiteList(request);
	}

	private boolean isWhiteList(HttpServletRequest request) {
		AntPathMatcher pathMatcher = new AntPathMatcher();
		return request.getMethod().equals(HttpMethod.GET.name())
			&& Arrays.stream(WHITE_LIST).anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()));
	}

	private boolean isAuthPath(String requestURI) {
		AntPathMatcher pathMatcher = new AntPathMatcher();
		return pathMatcher.match(AUTH_PATH, requestURI) || pathMatcher.match(LOGIN_PATH, requestURI);
	}
}

