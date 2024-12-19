package com.adit.backend.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.adit.backend.domain.auth.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
	void deleteByAccessToken(String accessToken);

	Optional<Token> findTokenByRefreshToken(String refreshToken);

	@Query("SELECT t FROM Token t JOIN FETCH t.user WHERE t.user.socialId = :socialId")
	Optional<Token> findByUserWithFetch(String socialId);

	Optional<Token> findByAccessToken(String accessToken);
}