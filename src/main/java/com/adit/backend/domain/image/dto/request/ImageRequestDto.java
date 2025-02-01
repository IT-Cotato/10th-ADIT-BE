package com.adit.backend.domain.image.dto.request;

import com.adit.backend.domain.event.entity.Event;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link Image}
 */
public record ImageRequestDto(@NotNull(message = "Place ID must not be null") CommonPlace commonPlace,
							  UserPlace userPlace,
							  Event event,
							  String url,
							  String fileName, String folderName) {
}