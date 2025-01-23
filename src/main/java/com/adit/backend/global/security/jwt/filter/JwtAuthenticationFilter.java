package com.adit.backend.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		try {
			String accessToken = tokenProvider.extractAccessToken(request).orElse(null);
			String refreshToken = tokenProvider.extractRefreshToken(request).orElse(null);

			if (accessToken != null) {
				processAccessToken(accessToken, refreshToken);
			}
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			log.error("JWT Filter Error: {}", e.getMessage());
			filterChain.doFilter(request, response);
		}
	}

	private void processAccessToken(String accessToken, String refreshToken) {
		try {
			if (tokenProvider.isAccessTokenValid(accessToken)) {
				setAuthentication(accessToken);
				return;
			}
			if (refreshToken != null && tokenProvider.isRefreshTokenValid(refreshToken)) {
				reissueAccessToken(refreshToken);
			}
		} catch (Exception e) {
			log.error("Token processing error: {}", e.getMessage());
		}
	}

	private void reissueAccessToken(String refreshToken) {
		try {
			String newAccessToken = tokenProvider.checkRefreshTokenAndReIssueAccessToken(null, refreshToken);
			if (newAccessToken != null) {
				setAuthentication(newAccessToken);
			}
		} catch (Exception e) {
			log.error("Token reissue error: {}", e.getMessage());
		}
	}

	private void setAuthentication(String accessToken) {
		try {
			Authentication authentication = tokenProvider.getAuthentication(accessToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.debug("Authentication set for user");
		} catch (Exception e) {
			log.error("Authentication setting error: {}", e.getMessage());
		}
	}
}

