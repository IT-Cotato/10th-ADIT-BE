package com.adit.backend.domain.auth.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.response.KakaoResponse;
import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.auth.repository.TokenRepository;
import com.adit.backend.global.error.exception.TokenException;

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
		tokenRepository.flush();
	}

	private Token validateAccessToken(KakaoResponse.TokenInfoDto response) {
		return tokenRepository.findByAccessToken(response.accessToken())
			.orElseGet(response::toEntity);
	}

	public Token validateRefreshToken(String refreshToken) {
		return tokenRepository.findTokenByRefreshToken(refreshToken)
			.orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND));
	}
}
