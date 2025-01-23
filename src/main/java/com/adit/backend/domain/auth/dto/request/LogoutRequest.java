package com.adit.backend.domain.auth.dto.request;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;

public record LogoutRequest(
	@NotNull
	String accessToken
) {
}