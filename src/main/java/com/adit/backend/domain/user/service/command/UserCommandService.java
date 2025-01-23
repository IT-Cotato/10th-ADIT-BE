package com.adit.backend.domain.user.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.user.converter.UserConverter;
import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.domain.user.service.query.UserQueryService;

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

	public UserResponse.InfoDto changeNickname(String accessToken, String nickname) {
		User user = userQueryService.findUserByAccessToken(accessToken);
		userQueryService.validateDuplicateNicknames(nickname);
		user.changeNickName(nickname);
		user.updateRole();
		return userConverter.InfoDto(user);
	}

	public User saveUserWithToken(User user, Token token) {
		user.addToken(token);
		log.info("Saving user: {}", user);
		return userRepository.save(user);
	}
}
