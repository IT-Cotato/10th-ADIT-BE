package com.adit.backend.domain.user.service.command;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.OAuth2UserInfo;
import com.adit.backend.domain.user.converter.UserConverter;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.UserException;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.domain.user.service.query.UserQueryService;
import com.adit.backend.global.error.GlobalErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCommandService {
	private final UserRepository userRepository;
	private final UserQueryService userQueryService;
	private final UserConverter userConverter;



	public UserResponse.InfoDto changeNickname(User user, String nickname) {
		userQueryService.validateDuplicateNicknames(nickname);
		user.changeNickName(nickname);
		user.updateRole();
		userRepository.save(user);
		return userConverter.InfoDto(user);
	}

	public UserResponse.InfoDto createOrUpdateUser(OAuth2UserInfo oAuth2UserInfo) {
		User user = userQueryService.findOrGetUserByOAuthInfo(oAuth2UserInfo);
		userRepository.save(user);
		log.info("[User] 유저 저장 완료 email : {}", user.getEmail());
		return userConverter.InfoDto(user);
	}




}



