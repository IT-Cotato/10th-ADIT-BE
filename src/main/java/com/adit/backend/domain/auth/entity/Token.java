package com.adit.backend.domain.auth.entity;

import java.time.LocalDateTime;

import com.adit.backend.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Token {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "social_id", referencedColumnName = "social_id")
	@JsonManagedReference
	private User user;

	private String accessToken;
	private String refreshToken;
	private LocalDateTime tokenExpiresAt;
	private LocalDateTime refreshTokenExpiresAt;

	public Token updateRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
		return this;
	}

	public void updateAccessToken(String accessToken, String tokenExpiresAt) {
		this.accessToken = accessToken;
		this.tokenExpiresAt = LocalDateTime.now().plusSeconds(Long.parseLong(tokenExpiresAt));
	}

	public void updateUserInfo(User user) {
		this.user = user;
	}
}