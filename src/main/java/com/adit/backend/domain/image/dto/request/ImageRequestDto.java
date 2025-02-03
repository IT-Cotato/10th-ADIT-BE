package com.adit.backend.domain.image.dto.request;

import com.adit.backend.domain.event.entity.Event;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for {@link Image}
 */
public record ImageRequestDto(CommonPlace commonPlace,
							  UserPlace userPlace,
							  Event event,
							  @NotBlank(message = "이미지 경로는 공백일 수 없습니다.") String url) {
}