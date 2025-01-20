package com.adit.backend.domain.ai.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record ContentListResponse(
	List<ContentResponse> contentResponseList,
	List<String> imageSrcList
) {
}
