package com.adit.backend.domain.auth.dto.response;

import com.adit.backend.domain.user.enums.Role;

import lombok.Builder;

@Builder
public record LoginResponse(Role role) {
	public static LoginResponse from(Role role) {
		return LoginResponse.builder().role(role).build();
	}
}