package com.adit.backend.global.security.jwt.entity;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@RedisHash(value = "jwtToken", timeToLive = 60 * 60 * 24 * 3) //만료시간 3일
public class RefreshToken {

	@Id
	private Long id; // 사용자 id

	private String refreshToken; // 사용자의 refreshToken

	public void updateRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
