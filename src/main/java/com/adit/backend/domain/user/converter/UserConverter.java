package com.adit.backend.domain.user.converter;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.user.dto.response.UserResponse;
import com.adit.backend.domain.user.entity.User;

@Component
public class UserConverter {
	public UserResponse.InfoDto InfoDto(User user) {
		return UserResponse.InfoDto.builder()
			.Id(user.getId())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.name(user.getName())
			.role(user.getRole())
			.build();
	}
}
