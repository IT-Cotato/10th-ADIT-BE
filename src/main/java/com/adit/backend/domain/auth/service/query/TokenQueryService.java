package com.adit.backend.domain.auth.service.query;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.auth.repository.TokenRepository;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenQueryService {

	public static final String DEFAULT_NICKNAME = "GUEST";
	private final TokenRepository tokenRepository;
	private final static String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
	private final RestTemplate restTemplate;

	private ResponseEntity<JsonNode> callKakaoApi(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> request = new HttpEntity<>(headers);
		return restTemplate.exchange(KAKAO_USER_INFO_URI, HttpMethod.GET, request, JsonNode.class);
	}

	public OAuth2UserInfo extractAccessToken(String accessToken) {
		ResponseEntity<JsonNode> response = callKakaoApi(accessToken);
		JsonNode userInfo = response.getBody();
		String name = userInfo.path("kakao_account").path("profile").path("nickname").asText();
		String email = userInfo.path("kakao_account").path("email").asText();
		String profileImageUrl = userInfo.path("kakao_account").path("profile").path("thumbnail_image_url").asText();
		return OAuth2UserInfo.builder()
			.name(name)
			.nickname(DEFAULT_NICKNAME)
			.email(email)
			.profile(profileImageUrl)
			.build();
	}

}