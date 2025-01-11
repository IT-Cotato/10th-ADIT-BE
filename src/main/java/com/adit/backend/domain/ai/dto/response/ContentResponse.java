package com.adit.backend.domain.ai.dto.response;

import com.adit.backend.domain.ai.enums.ContentType;
public record ContentResponse(
	String name,
	ContentType type,
	String location,
	String period
) {
	public static ContentResponse of(String name, String location, ContentType type, String period) {
		return new ContentResponse(name, type, location, period);
	}
}
