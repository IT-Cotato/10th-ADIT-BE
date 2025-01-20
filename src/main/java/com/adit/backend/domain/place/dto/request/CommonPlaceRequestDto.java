package com.adit.backend.domain.place.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;

import com.adit.backend.domain.place.entity.CommonPlace;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link CommonPlace}
 */
public record CommonPlaceRequestDto(@NotNull(message = "Place name must not be null") String placeName,
									BigDecimal latitude, BigDecimal longitude, String addressName,
									String roadAddressName, String subCategory, String url)
	implements Serializable {
}