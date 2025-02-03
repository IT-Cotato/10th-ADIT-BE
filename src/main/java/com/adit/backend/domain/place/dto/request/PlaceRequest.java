package com.adit.backend.domain.place.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.adit.backend.domain.place.entity.CommonPlace;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link CommonPlace}
 */
public record PlaceRequest(@NotNull(message = "Place name must not be null") String placeName,
						   BigDecimal latitude, BigDecimal longitude, String addressName,
						   String roadAddressName, String subCategory, String url,
						   String memo, List<String> imageUrlList)
	implements Serializable {
}