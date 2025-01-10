package com.adit.backend.domain.ai.dto.response;

import java.util.List;

public record ContentListResponse(
	List<ContentResponse> contentResponseList
) {
}
