package com.adit.backend.domain.place.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link CommonPlace}
 */
public record CommonPlaceResponseDto(@NotNull(message = "Place ID must not be null") Long id, String placeName,
									 BigDecimal latitude, BigDecimal longitude, String addressName,
									 String roadAddressName, String subCategory, String url,
									 List<String> imageUrl) {
	public static CommonPlaceResponseDto from(CommonPlace place) {
		return new CommonPlaceResponseDto(
			place.getId(),
			place.getPlaceName(),
			place.getLatitude(),
			place.getLongitude(),
			place.getAddressName(),
			place.getRoadAddressName(),
			place.getSubCategory(),
			place.getUrl(),
			place.getImages().stream().map(Image::getUrl).collect(Collectors.toList())
		);
	}

	public static List<CommonPlaceResponseDto> from(List<CommonPlace> places){
		 return places.stream()
			.map(CommonPlaceResponseDto::from)
			.toList();

	}
}