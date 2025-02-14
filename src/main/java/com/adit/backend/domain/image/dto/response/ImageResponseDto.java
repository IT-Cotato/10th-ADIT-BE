package com.adit.backend.domain.image.dto.response;

import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for {@link Image}
 */
@Builder
public record ImageResponseDto(@NotNull(message = "이미지 ID는 null일 수 없습니다.") Long id,
							   @Nullable CommonPlace commonPlace,
							   @Nullable UserPlace userPlace,

							   @Nullable UserEvent userEvent,
							   @Nullable CommonEvent commonEvent,
							   @NotBlank(message = "이미지 주소는 공백일 수 없습니다.") String url) {
}