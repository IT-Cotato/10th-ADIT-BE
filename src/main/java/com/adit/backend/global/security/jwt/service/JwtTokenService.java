package com.adit.backend.global.security.jwt.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.auth.repository.TokenRepository;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.exception.BusinessException;
import com.adit.backend.global.error.exception.TokenException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtTokenService {

	@Value("${token.access.expiration}")
	String accessExpirationAt;

	@Value("${token.refresh.expiration}")
	String refreshExpirationAt;
	private final TokenRepository tokenRepository;
	private final UserRepository userRepository;

	@Transactional
	public void saveOrUpdate(String email, String refreshToken, String accessToken) {
		log.info("Processing token saveOrUpdate for email: {}", email);
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

		tokenRepository.findByUserWithFetch(email)
			.ifPresentOrElse(
				token -> {
					log.info("발급된 토큰이 존재합니다. 업데이트합니다.");
					updateTokens(accessToken, refreshToken, token);
				},
				() -> {
					log.info("발급된 토큰이 존재하지 않습니다 발급합니다.");
					Token newToken = Token.builder()
						.user(user)
						.build();
					updateTokens(accessToken, refreshToken, newToken);
					tokenRepository.save(newToken);
				}
			);
	}

	public Optional<Token> findByAccessTokenOrThrow(String refreshToken) {
		return Optional.ofNullable(tokenRepository.findTokenByRefreshToken(refreshToken)
			.orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND)));
	}

	public void updateTokens(String accessToken, String refreshToken, Token token) {
		token.updateAccessToken(accessToken, accessExpirationAt);
		token.updateRefreshToken(refreshToken, refreshExpirationAt);
	}
}