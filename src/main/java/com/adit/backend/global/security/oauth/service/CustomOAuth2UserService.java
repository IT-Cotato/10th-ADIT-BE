package com.adit.backend.global.security.oauth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final UserQueryService userQueryService;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);
		// 예외 처리 추가
		try {
			return this.process(userRequest, oauth2User);
		} catch (Exception ex) {
			log.error("OAuth2 로그인 처리 중 에러 발생: {}", ex.getMessage());
			throw new OAuth2AuthenticationException(ex.getMessage());
		}
	}

	private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, oauth2User.getAttributes());
		User user = getOrSaveUser(userInfo);

		return new PrincipalDetails(user, oauth2User.getAttributes(), userNameAttributeName);
	}

	private User getOrSaveUser(OAuth2UserInfo oAuth2UserInfo) {
		User user = userQueryService.findUserByOAuthInfo(oAuth2UserInfo);
		user.decideSocialType();
		return userRepository.save(user);
	}
}


