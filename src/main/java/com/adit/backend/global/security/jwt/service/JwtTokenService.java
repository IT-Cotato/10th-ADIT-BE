package com.adit.backend.global.security.jwt.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;

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
@Transactional
public class JwtTokenService {

	private static final String KAKAO_LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";
	private final TokenRepository tokenRepository;
	private final UserRepository userRepository;

	public void saveOrUpdate(String socialId, String refreshToken, String accessToken) {
		log.info("Processing token saveOrUpdate for socialId: {}", socialId);
		User user = userRepository.findBySocialId(socialId)
			.orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

		tokenRepository.findByUserWithFetch(socialId)
			.ifPresentOrElse(
				token -> {
					log.info("발급된 토큰이 존재합니다. 업데이트합니다.");
					token.updateRefreshToken(refreshToken);
					token.updateAccessToken(accessToken, "");
				},
				() -> {
					log.info("발급된 토큰이 존재하지 않습니다 발급합니다.");
					Token newToken = Token.builder()
						.user(user)
						.refreshToken(refreshToken)
						.accessToken(accessToken)
						.build();
					tokenRepository.save(newToken);
				}
			);
		log.info("Token successfully saved or updated for socialId: {}", socialId);
	}

	public Token findByAccessTokenOrThrow(String refreshToken) {
		return tokenRepository.findTokenByRefreshToken(refreshToken)
			.orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND));
	}

	public void updateAccessToken(String accessToken, Token token) {
		token.updateAccessToken(accessToken, "");
	}

	public void updateRefreshToken(String refreshToken, Token token) {
		token.updateRefreshToken(refreshToken);
	}

	public void deleteToken(String accessToken) {
		tokenRepository.deleteByAccessToken(accessToken);
	}

}