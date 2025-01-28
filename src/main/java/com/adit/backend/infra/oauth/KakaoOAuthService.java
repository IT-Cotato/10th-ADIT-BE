package com.adit.backend.infra.oauth;

import static com.adit.backend.global.error.GlobalErrorCode.*;
import static org.springframework.http.MediaType.*;

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

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.auth.dto.response.KakaoResponse;
import com.adit.backend.global.security.jwt.exception.AuthException;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoOAuthService {
	public static final String KAKAO_ACCOUNT_PATH = "kakao_account";
	private final RestTemplate restTemplate;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String clientSecret;

	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String redirectUri;

	@Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
	private String tokenUri;

	@Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
	private String userInfoUri;

	private static HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(APPLICATION_JSON));
		return headers;
	}

	public ResponseEntity<KakaoResponse.TokenInfoDto> requestTokenIssuance(String code) {
		HttpHeaders headers = createHeaders();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("redirect_uri", redirectUri);
		params.add("code", code);
		log.info("[User] 인가코드 -> 카카오 액세스 토큰 발급 완료");
		return request(tokenUri, new HttpEntity<>(params, headers), KakaoResponse.TokenInfoDto.class);
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

	private ResponseEntity<JsonNode> callUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> request = new HttpEntity<>(headers);
		return restTemplate.exchange(userInfoUri, HttpMethod.GET, request, JsonNode.class);
	}

	public OAuth2UserInfo requestOAuth2UserInfo(String accessToken) {
		ResponseEntity<JsonNode> response = callUserInfo(accessToken);
		JsonNode userInfo = response.getBody();
		String name = userInfo.path(KAKAO_ACCOUNT_PATH).path("profile").path("nickname").asText();
		String email = userInfo.path(KAKAO_ACCOUNT_PATH).path("email").asText();
		String profileImageUrl = userInfo.path(KAKAO_ACCOUNT_PATH).path("profile").path("thumbnail_image_url").asText();
		log.info("[User] 카카오 액세스 토큰 -> 유저 정보 반환 완료");
		return OAuth2UserInfo.from(name, email, profileImageUrl);
	}

}

