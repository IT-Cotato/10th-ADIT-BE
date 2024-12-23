package com.adit.backend.domain.user.dto.request;

import org.hibernate.validator.constraints.Length;

public record UserRequest() {

	public record NicknameDto(@Length(min = 2, max = 12) String nickname) {

	}
}
