package com.adit.backend.domain.event.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record EventRequestDto(
    @NotBlank String name,
    @NotBlank String category,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String memo,
    Boolean visited,
	List<String> imageUrlList
) {}