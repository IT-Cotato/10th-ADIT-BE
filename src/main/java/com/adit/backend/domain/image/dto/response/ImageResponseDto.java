package com.adit.backend.domain.image.dto.response;

import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO for {@link Image}
 */
@Builder
public record ImageResponseDto(@NotNull(message = "이미지 ID는 null일 수 없습니다.") Long id,
							   CommonPlace commonPlace,
							   UserPlace userPlace,
							   UserEvent userEvent,
							   String url) {
	public static ImageResponseDto from(Image image) {
		return ImageResponseDto.builder()
			.id(image.getId())
			.commonPlace(image.getCommonPlace())
			.userPlace(image.getUserPlace())
			.userEvent(image.getUserEvent())
			.url(image.getUrl())
			.build();
	}
}