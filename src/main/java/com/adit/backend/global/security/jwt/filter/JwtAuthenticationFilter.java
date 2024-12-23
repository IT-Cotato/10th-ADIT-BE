package com.adit.backend.global.security.jwt.filter;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.domain.user.principal.PrincipalDetailsService;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.exception.BusinessException;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider tokenProvider;
	private final UserRepository userRepository;
	private final PrincipalDetailsService principalDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) {
		try {
			String accessToken = tokenProvider.extractAccessToken(request)
				.filter(tokenProvider::isAccessTokenValid)
				.orElse(null);
			String refreshToken = tokenProvider.extractRefreshToken(request)
				.filter(tokenProvider::isRefreshTokenValid)
				.orElse(null);

			if (refreshToken != null && (accessToken == null || !tokenProvider.isAccessTokenValid(accessToken))) {
				String newAccessToken = tokenProvider.checkRefreshTokenAndReIssueAccessToken(
					tokenProvider.getAuthentication(accessToken), refreshToken);
				setAuthentication(newAccessToken);
				return;
			} else if (refreshToken == null) {
				checkAccessTokenAndAuthentication(request, response, filterChain);
			}
		} catch (ServletException e) {
			throw new BusinessException(SERVLET_ERROR);
		} catch (IOException e) {
			throw new BusinessException(IO_ERROR);
		}
	}

	private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		log.info("checkAccessTokenAndAuthentication() 호출");
		tokenProvider.extractAccessToken(request)
			.filter(tokenProvider::isAccessTokenValid)
			.ifPresent(accessToken -> tokenProvider.getSocialId(accessToken)
				.ifPresent(socialId -> userRepository.findBySocialId(socialId)
					.ifPresent(user -> setAuthentication(accessToken))));
		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String accessToken) {
		String socialId = tokenProvider.getSocialId(accessToken).orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND));
		PrincipalDetails principalDetails = principalDetailsService.loadUserByUsername(socialId);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			principalDetails,
			null,
			principalDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
