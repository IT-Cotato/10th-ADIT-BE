package com.adit.backend.domain.auth.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.auth.dto.request.KakaoRequest;
import com.adit.backend.domain.auth.dto.response.KakaoResponse;
import com.adit.backend.domain.auth.dto.response.LoginResponse;
import com.adit.backend.domain.auth.dto.response.ReissueResponse;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.domain.user.service.command.UserCommandService;
import com.adit.backend.global.error.exception.BusinessException;
import com.adit.backend.global.security.jwt.entity.RefreshToken;
import com.adit.backend.global.security.jwt.entity.Token;
import com.adit.backend.global.security.jwt.repository.BlackListRepository;
import com.adit.backend.global.security.jwt.repository.RefreshTokenRepository;
import com.adit.backend.global.security.jwt.service.JwtTokenService;
import com.adit.backend.global.security.jwt.util.JwtTokenProvider;
import com.adit.backend.infra.oauth.KakaoOAuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthService {
	private final JwtTokenProvider tokenProvider;
	private final JwtTokenService jwtTokenService;
	private final KakaoOAuthService kakaoOAuthService;
	private final UserCommandService userCommandService;
	private final BlackListRepository blackListRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${token.refresh.expiration}")
	private String refreshTokenExpiresAt;

	@Value("${token.refresh.cookie.name}")
	private String refreshTokenCookieName;

	//로그인
	public LoginResponse login(KakaoRequest.AuthDto request, HttpServletResponse response) {
		KakaoResponse.TokenInfoDto kakaoTokenInfo = kakaoOAuthService.requestTokenIssuance(request.code()).getBody();
		OAuth2UserInfo oAuth2UserInfo = kakaoOAuthService.requestOAuth2UserInfo(kakaoTokenInfo.accessToken());
		UserResponse.InfoDto infoDto = userCommandService.createOrUpdateUser(oAuth2UserInfo);

		return LoginResponse.from(infoDto.role());
	}

	// 토큰 재발급
	public ReissueResponse reIssue(String refreshToken, HttpServletResponse response) {
		if (!tokenProvider.isRefreshTokenValid(refreshToken) || blackListRepository.existsById(refreshToken)) {
			log.warn("[Token] 블랙리스트에 존재하는 토큰입니다.]: {}", blackListRepository.existsById(refreshToken));
			throw new BusinessException(NOT_VALID_ERROR);
		}
		Authentication authentication = tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
		PrincipalDetails userDetails = tokenProvider.getUserDetails(authentication);
		RefreshToken findToken = refreshTokenRepository.findById(userDetails.getUser().getId())
			.orElseThrow(() -> new BusinessException(TOKEN_NOT_FOUND));

		log.info("[Token] 동일한 토큰입니다. Cookie == DB : {}", refreshToken.equals(findToken.getRefreshToken()));
		if (!refreshToken.equals(findToken.getRefreshToken())) {
			log.warn("[Token] 동일하지 않은 토큰입니다. Cookie != DB | refreshToken: {}, findToken : {}", refreshToken, findToken);
			throw new BusinessException(TOKEN_NOT_FOUND);
		}
		jwtTokenService.setBlackList(refreshToken);
		Token token = tokenProvider.createToken(authentication);
		findToken.updateRefreshToken(token.getRefreshToken());
		refreshTokenRepository.save(findToken);

		addRefreshTokenToCookie(token.getRefreshToken(), response);
		return ReissueResponse.from(token.getAccessToken());
	}

	// 토큰 삭제 및 로그아웃
	public void logout(String refreshToken, HttpServletResponse response) {
		Authentication authentication = tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
		PrincipalDetails userDetails = tokenProvider.getUserDetails(authentication);
		RefreshToken existRefreshToken = refreshTokenRepository.findById(userDetails.getUser().getId())
			.orElseThrow(() -> new BusinessException(TOKEN_NOT_FOUND));
		jwtTokenService.setBlackList(refreshToken);
		refreshTokenRepository.delete(existRefreshToken);
		addRefreshTokenToCookie(null, response);
		log.info("[Logout] 로그아웃 완료");
	}

	private void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
		Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
		if (cookie.getValue() == null) {
			cookie.setMaxAge(0);
		}
		cookie.setPath("/");
		ZonedDateTime seoulTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		ZonedDateTime expirationTime = seoulTime.plusSeconds(Long.parseLong(refreshTokenExpiresAt));
		cookie.setMaxAge((int)(expirationTime.toEpochSecond() - seoulTime.toEpochSecond()));
		//cookie.setSecure(true);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		log.info("[Token] RefreshToken 생성 완료: {}", cookie.getValue());
	}

}

