package com.adit.backend.domain.event.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record EventResponseDto(
	Long id,
	String name,
	String category,
	LocalDateTime startDate,
	LocalDateTime endDate,
	String memo,
	Boolean visited,
	String url) {
}