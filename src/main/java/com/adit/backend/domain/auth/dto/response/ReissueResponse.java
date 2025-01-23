package com.adit.backend.domain.auth.dto.response;

public record ReissueResponse(
	String accessToken
) {
	public static ReissueResponse from(String accessToken) {
		return new ReissueResponse(accessToken);
	}
}