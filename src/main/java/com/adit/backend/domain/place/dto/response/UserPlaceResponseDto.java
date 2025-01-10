package com.adit.backend.domain.place.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;

public record UserPlaceResponseDto(@NotNull(message = "place ID must not be null") Long id, String memo
										, boolean visited, String placeName, String subCategory, String RoadAddressName
										, List<String> imageUrl) {


	public static UserPlaceResponseDto from(UserPlace userPlace){
		return new UserPlaceResponseDto(
			userPlace.getId(),
			userPlace.getMemo(),
			userPlace.getVisited(),
			userPlace.getCommonPlace().getPlaceName(),
			userPlace.getCommonPlace().getSubCategory(),
			userPlace.getCommonPlace().getRoadAddressName(),
			userPlace.getCommonPlace().getImages().stream().map(Image::getUrl).collect(Collectors.toList())
		);
	}

	public static List<UserPlaceResponseDto> from(List<UserPlace> places){
		return places.stream()
			.map(UserPlaceResponseDto::from)
			.toList();

	}

}
