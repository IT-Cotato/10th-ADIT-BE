package com.adit.backend.domain.auth.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.dto.response.KakaoResponse;
import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.auth.repository.TokenRepository;
import com.adit.backend.global.error.exception.TokenException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenQueryService {
	private final TokenRepository tokenRepository;

	private Token validateAccessToken(KakaoResponse.TokenInfoDto response) {
		return tokenRepository.findByAccessToken(response.accessToken())
			.orElseGet(response::toEntity);
	}

	public Token validateRefreshToken(String refreshToken) {
		return tokenRepository.findTokenByRefreshToken(refreshToken)
			.orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND));
	}

}