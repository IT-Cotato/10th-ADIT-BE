package com.adit.backend.domain.auth.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.auth.repository.TokenRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenCommandService {

	private final TokenRepository tokenRepository;

	public void deleteToken(Token token) {
		tokenRepository.delete(token);
		log.info("[토큰 삭제 완료] accessToken : {}", token.getUser().getEmail());
	}
}
