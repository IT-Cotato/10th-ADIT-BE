package com.adit.backend.domain.auth.dto.response;

import java.time.LocalDateTime;

import com.adit.backend.domain.auth.entity.Token;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoResponse() {
	public record TokenInfoDto(@JsonProperty("token_type") String tokenType,
							   @JsonProperty("access_token") String accessToken,
							   @JsonProperty("id_token") String idToken,
							   @JsonProperty("expires_in") Integer expiresIn,
							   @JsonProperty("refresh_token") String refreshToken,
							   @JsonProperty("refresh_token_expires_in") Integer refreshTokenExpiresIn,
							   @JsonProperty("scope") String scope
	) {
		public Token toEntity() {
			return Token.builder()
				.accessToken(accessToken)
				.tokenExpiresAt(LocalDateTime.now().plusSeconds(this.expiresIn()))
				.refreshToken(refreshToken)
				.refreshTokenExpiresAt(LocalDateTime.now().plusSeconds(this.refreshTokenExpiresIn()))
				.build();
		}
	}

	@Builder
	public record AccessTokenDto(
		@JsonProperty("access_token")
		String accessToken,

		@JsonProperty("expires_in")
		String expiresIn
	) {
	}

	public record UserIdDto(Long id) {
	}

}
