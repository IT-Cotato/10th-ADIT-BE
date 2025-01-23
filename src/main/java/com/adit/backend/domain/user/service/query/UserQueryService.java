package com.adit.backend.domain.user.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.user.UserException;
import com.adit.backend.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQueryService {

	private final UserRepository userRepository;
	public static final String DEFAULT_NICKNAME = "GUEST";

	public User findUserByAccessToken(String accessToken) {
		return userRepository.findUserByToken_AccessToken(accessToken)
			.orElseThrow(() -> new UserException(USER_NOT_FOUND));
	}

	public User findUserByOAuthInfo(OAuth2UserInfo oAuth2UserInfo) {
		return userRepository.findByEmail(oAuth2UserInfo.email())
			.orElseGet(oAuth2UserInfo::toEntity);
	}

	public void validateDuplicateNicknames(String nickname) {
		if (nickname == null || nickname.trim().isEmpty()) {
			throw new UserException(NULL_POINT_ERROR);
		} else if (userRepository.existsByNickname(nickname)) {
			throw new UserException(NICKNAME_ALREADY_EXIST);
		}
	}
}
