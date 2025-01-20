package com.adit.backend.domain.ai.dto.response;

import com.adit.backend.domain.ai.enums.ContentType;


public record ContentResponse(
	String name,
	ContentType type,
	String location,
	String period
) {
}
