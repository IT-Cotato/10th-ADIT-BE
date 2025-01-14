package com.adit.backend.domain.place.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPlaceResponseDto(@NotNull(message = "place ID must not be null") Long id, @Nullable String memo
										, boolean visited, String placeName, String subCategory, String RoadAddressName
										, List<String> imageUrl, @Nullable Long userId, @Nullable String profile) {


	public static UserPlaceResponseDto from(UserPlace userPlace){
		return new UserPlaceResponseDto(
			userPlace.getId(),
			userPlace.getMemo(),
			userPlace.getVisited(),
			userPlace.getCommonPlace().getPlaceName(),
			userPlace.getCommonPlace().getSubCategory(),
			userPlace.getCommonPlace().getRoadAddressName(),
			userPlace.getCommonPlace().getImages().stream().map(Image::getUrl).collect(Collectors.toList()),
			null,
			null
		);
	}

	//친구 기반 장소 조회를 위한 Dto
	public static UserPlaceResponseDto of(UserPlace userPlace){
		return new UserPlaceResponseDto(
			userPlace.getId(),
			userPlace.getMemo(),
			userPlace.getVisited(),
			userPlace.getCommonPlace().getPlaceName(),
			userPlace.getCommonPlace().getSubCategory(),
			userPlace.getCommonPlace().getRoadAddressName(),
			userPlace.getCommonPlace().getImages().stream().map(Image::getUrl).collect(Collectors.toList()),
			userPlace.getUser().getId(),
			userPlace.getUser().getProfile()
		);
	}


	public static List<UserPlaceResponseDto> from(List<UserPlace> places){
		return places.stream()
			.map(UserPlaceResponseDto::from)
			.toList();

	}

	public static List<UserPlaceResponseDto> of(List<UserPlace> places){
		return places.stream()
			.map(UserPlaceResponseDto::of)
			.toList();

	}

}
