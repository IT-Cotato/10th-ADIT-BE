package com.adit.backend.domain.auth.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;
import static org.springframework.http.MediaType.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.auth.dto.response.KakaoResponse;
import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.auth.service.query.TokenQueryService;
import com.adit.backend.domain.user.converter.UserConverter;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.command.UserCommandService;
import com.adit.backend.domain.user.service.query.UserQueryService;
import com.adit.backend.global.error.exception.TokenException;
import com.adit.backend.global.security.jwt.exception.AuthException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthCommandService {
	public static final String ACCESS_TOKEN_HEADER = "Authorization";
	public static final String AUTH_CODE_GRANT = "authorization_code";
	public static final String REFRESH_TOKEN_GRANT = "refresh_token";
	public static final String RESPONSE_TYPE = "code";

	private final RestTemplate restTemplate;
	private final TokenCommandService tokenCommandService;
	private final TokenQueryService tokenQueryService;
	private final UserQueryService userQueryService;
	private final UserCommandService userCommandService;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String clientSecret;

	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String redirectUri;

	@Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
	private String authorizationUri;

	@Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
	private String tokenUri;

	@Value("${kakao.logout-url}")
	private String logoutUrl;

	@Value("${token.refresh.expiration}")
	private int refreshTokenExpiresIn;

	public String createKakaoAuthorizationUrl() {
		return UriComponentsBuilder
			.fromUriString(authorizationUri)
			.queryParam("client_id", clientId)
			.queryParam("redirect_uri", redirectUri)
			.queryParam("response_type", RESPONSE_TYPE)
			.build()
			.toUriString();
	}

	private static HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(APPLICATION_JSON));
		return headers;
	}

	public UserResponse.InfoDto joinAuth(String code, HttpServletResponse response) {
		KakaoResponse.TokenInfoDto tokenInfoDto = executeKakaoLoginRequest(code).getBody();
		tokenCommandService.saveOrUpdateToken(tokenInfoDto);
		response.setHeader(ACCESS_TOKEN_HEADER, tokenInfoDto.accessToken());
		addRefreshTokenToCookie(tokenInfoDto.refreshToken(), response);
		return login(tokenInfoDto.accessToken());
	}

	public KakaoResponse.AccessTokenDto reIssueKakaoToken(String refreshToken, HttpServletResponse response) {
		KakaoResponse.AccessTokenDto accessTokenDto = executeReissueTokenRequest(refreshToken).getBody();
		tokenCommandService.updateTokenEntity(accessTokenDto, refreshToken);
		addRefreshTokenToCookie(refreshToken, response);
		return accessTokenDto;
	}

	public KakaoResponse.UserIdDto logout(String accessToken, HttpServletResponse response) {
		return executeKakaoLogoutRequest(accessToken.replace("Bearer ", ""), response);
	}

	private UserResponse.InfoDto login(String accessToken) {
		OAuth2UserInfo oAuth2UserInfo = tokenQueryService.extractAccessToken(accessToken);
		User user = userQueryService.findUserByOAuthInfo(oAuth2UserInfo);
		Token token = tokenQueryService.findTokenByAccessToken(accessToken);
		userCommandService.saveUserWithToken(user, token);
		log.info("[로그인 성공] Email : {}", user.getEmail());
		return UserConverter.InfoDto(user);
	}

	private void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
		Cookie cookie = new Cookie(REFRESH_TOKEN_GRANT, refreshToken);
		cookie.setPath("/");
		ZonedDateTime seoulTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		ZonedDateTime expirationTime = seoulTime.plusSeconds(refreshTokenExpiresIn);
		cookie.setMaxAge((int)(expirationTime.toEpochSecond() - seoulTime.toEpochSecond()));
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		log.info("[쿠키 생성 완료] Cookie: {}", cookie.getValue());
	}

	private KakaoResponse.UserIdDto executeKakaoLogoutRequest(String accessToken, HttpServletResponse response) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		try {
			KakaoResponse.UserIdDto userIdDto = restTemplate.exchange(
				logoutUrl,
				HttpMethod.POST,
				new HttpEntity<>(headers),
				KakaoResponse.UserIdDto.class
			).getBody();
			deleteRefreshTokenToCookie(accessToken, response);
			log.info("[로그아웃 성공] : {}", accessToken);
			return userIdDto;
		} catch (Exception e) {
			log.error("로그아웃 실패: {}", e.getMessage());
			throw new TokenException(LOGOUT_FAILED);
		}
	}

	private void deleteRefreshTokenToCookie(String accessToken, HttpServletResponse response) {
		Cookie deleteCookie = new Cookie(REFRESH_TOKEN_GRANT, null);
		deleteCookie.setMaxAge(0);
		deleteCookie.setPath("/");
		deleteCookie.setSecure(true);
		deleteCookie.setHttpOnly(true);
		response.addCookie(deleteCookie);
		tokenCommandService.deleteToken(accessToken);
		log.info("[쿠키 생성 완료] deleteCookie: {}", deleteCookie.getValue());
	}

	private ResponseEntity<KakaoResponse.TokenInfoDto> executeKakaoLoginRequest(String code) {
		HttpHeaders headers = createHeaders();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", AUTH_CODE_GRANT);
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("redirect_uri", redirectUri);
		params.add("code", code);
		return request(tokenUri, new HttpEntity<>(params, headers), KakaoResponse.TokenInfoDto.class);
	}

	private ResponseEntity<KakaoResponse.AccessTokenDto> executeReissueTokenRequest(String refreshToken) {
		HttpHeaders headers = createHeaders();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", REFRESH_TOKEN_GRANT);
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("refresh_token", refreshToken);
		return request(tokenUri, new HttpEntity<>(params, headers), KakaoResponse.AccessTokenDto.class);
	}

	private <T> ResponseEntity<T> request(String url, HttpEntity<?> entity, Class<T> responseType) {
		try {
			return restTemplate.postForEntity(url, entity, responseType);
		} catch (HttpClientErrorException e) {
			log.error("Kakao API client error: {}", e.getStatusCode());
			throw new AuthException(INVALID_TOKEN);
		} catch (HttpServerErrorException e) {
			log.error("Kakao API server error: {}", e.getStatusCode());
			throw new AuthException(KAKAO_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Kakao API request failed: {}", e.getMessage());
			throw new AuthException(API_REQUEST_FAILED);
		}
	}
}

