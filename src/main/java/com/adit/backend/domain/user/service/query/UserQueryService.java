package com.adit.backend.domain.user.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.user.NickNameNullException;
import com.adit.backend.domain.user.exception.user.NickNameValidateException;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQueryService {

	private final UserRepository userRepository;

	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
	}

	public User findOrGetUserByOAuthInfo(OAuth2UserInfo oAuth2UserInfo) {
		return userRepository.findByEmail(oAuth2UserInfo.email())
			.orElseGet(oAuth2UserInfo::toEntity);
	}

	public void validateDuplicateNicknames(String nickname) {
		if (nickname == null || nickname.trim().isEmpty()) {
			throw new NickNameNullException("Nickname is null");
		} else if (userRepository.existsByNickname(nickname)) {
			throw new NickNameValidateException("NickName is already exist");
		}
	}
}
