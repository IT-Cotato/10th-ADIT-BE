package com.adit.backend.domain.user.dto.response;

import com.adit.backend.domain.user.enums.Role;

import lombok.Builder;

public record UserResponse() {

	@Builder
	public record InfoDto(Long Id, String email, String name, String nickname, Role role) {
	}

}