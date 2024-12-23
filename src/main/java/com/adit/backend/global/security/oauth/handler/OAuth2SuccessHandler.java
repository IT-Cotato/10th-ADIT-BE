package com.adit.backend.global.security.oauth.handler;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.global.error.exception.BusinessException;
import com.adit.backend.global.security.jwt.service.JwtTokenService;
import com.adit.backend.global.security.jwt.util.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private static final String REDIRECT_URI = "/api/auth/kakao";
	@Value("${jwt.access.expiration}")
	private Long accessTokenExpirationPeriod;

	private final JwtTokenProvider tokenProvider;
	private final JwtTokenService jwtTokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) {
		try {

			PrincipalDetails userDetails = getUserDetails(authentication);
			String accessToken = tokenProvider.generateAccessToken(authentication);
			String refreshToken = tokenProvider.generateRefreshToken(authentication);

			jwtTokenService.saveOrUpdate(userDetails.getUsername(), refreshToken, accessToken);
			tokenProvider.sendAccessAndRefreshToken(response, accessToken, refreshToken);

			String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
				.queryParam("accessToken", accessToken)
				.queryParam("refreshToken", refreshToken)
				.build().toUriString();

			log.info("로그인에 성공하였습니다. 유저 Social ID : {}", userDetails.getUsername());
			log.info("로그인에 성공하였습니다. AccessToken : {}", accessToken);
			log.info("발급된 AccessToken 만료 기간 : {}", accessTokenExpirationPeriod);
			response.sendRedirect(targetUrl);
		} catch (IOException e) {
			throw new BusinessException(IO_ERROR);
		}
	}

	private PrincipalDetails getUserDetails(Authentication authentication) {
		return (PrincipalDetails)authentication.getPrincipal();
	}

}