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

	public Token saveOrUpdateToken(KakaoResponse.TokenInfoDto response) {
		Token token = validateAccessToken(response);
		log.info("Saving token: {}", token);
		return tokenRepository.save(token);
	}

	public Token updateTokenEntity(KakaoResponse.AccessTokenDto response, String refreshToken) {
		Token token = validateRefreshToken(refreshToken);
		token.updateAccessToken(response.accessToken(), response.expiresIn());
		log.info("Updating token: {}", token);
		return tokenRepository.save(token);
	}

	public void deleteToken(String tokenValue) {
		tokenRepository.deleteByAccessToken(tokenValue);
		tokenRepository.flush();
	}

	private Token validateAccessToken(KakaoResponse.TokenInfoDto response) {
		return tokenRepository.findByAccessToken(response.accessToken())
			.orElseGet(response::toEntity);

	}

	private Token validateRefreshToken(String refreshToken) {
		return tokenRepository.findTokenByRefreshToken(refreshToken)
			.orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND));
	}
}
