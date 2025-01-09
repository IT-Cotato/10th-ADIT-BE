package com.adit.backend.domain.place.dto.response;

import javax.validation.constraints.NotNull;

import com.adit.backend.domain.place.entity.UserPlace;

public record PlaceByCategoryResponseDto(@NotNull(message = "place ID must not be null") Long id, String memo
										, boolean visited, String placeName, String subCategory, String RoadAddressName ) {


	public static PlaceByCategoryResponseDto from(UserPlace userPlace){
		return new PlaceByCategoryResponseDto(
			userPlace.getId(),
			userPlace.getMemo(),
			userPlace.getVisited(),
			userPlace.getCommonPlace().getPlaceName(),
			userPlace.getCommonPlace().getSubCategory(),
			userPlace.getCommonPlace().getRoadAddressName()
		);
	}
}
