package com.adit.backend.domain.place.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.annotation.Nullable;
import software.amazon.ion.Decimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaceResponseDto(@NotNull(message = "place ID must not be null") Long commonPlaceId, @Nullable Long userPlaceId, @Nullable String memo
	,@Nullable Boolean visited, String placeName, @Nullable BigDecimal latitude, @Nullable BigDecimal longitude, String subCategory, String RoadAddressName
	, @Nullable String AddressName, @Nullable String url, List<String> imageUrl, @Nullable Long friendUserId, @Nullable String profile){

	public static PlaceResponseDto userPlace(UserPlace userPlace){
		return new PlaceResponseDto(
			userPlace.getCommonPlace().getId(),
			userPlace.getId(),
			userPlace.getMemo(),
			userPlace.getVisited(),
			userPlace.getCommonPlace().getPlaceName(),
			null,
			null,
			userPlace.getCommonPlace().getSubCategory(),
			userPlace.getCommonPlace().getRoadAddressName(),
			userPlace.getCommonPlace().getAddressName(),
			null,
			userPlace.getCommonPlace().getImages().stream().map(Image::getUrl).collect(Collectors.toList()),
			null,
			null
		);
	}

	public static List<PlaceResponseDto> userPlace(List<UserPlace> places){
		return places.stream()
			.map(PlaceResponseDto::userPlace)
			.toList();

	}
	//친구 기반 장소 조회
	public static PlaceResponseDto friend(UserPlace userPlace){
		return new PlaceResponseDto(
			userPlace.getCommonPlace().getId(),
			userPlace.getId(),
			userPlace.getMemo(),
			userPlace.getVisited(),
			userPlace.getCommonPlace().getPlaceName(),
			null,
			null,
			userPlace.getCommonPlace().getSubCategory(),
			userPlace.getCommonPlace().getRoadAddressName(),
			userPlace.getCommonPlace().getAddressName(),
			null,
			userPlace.getCommonPlace().getImages().stream().map(Image::getUrl).collect(Collectors.toList()),
			userPlace.getUser().getId(),
			userPlace.getUser().getProfile()
		);
	}

	public static List<PlaceResponseDto> friend(List<UserPlace> places){
		return places.stream()
			.map(PlaceResponseDto::friend)
			.toList();

	}

	public static PlaceResponseDto commonPlace(CommonPlace place) {
		return new PlaceResponseDto(
			place.getId(),
			null,
			null,
			null,
			place.getPlaceName(),
			place.getLatitude(),
			place.getLongitude(),
			place.getSubCategory(),
			place.getRoadAddressName(),
			place.getAddressName(),
			place.getUrl(),
			place.getImages().stream().map(Image::getUrl).collect(Collectors.toList()),
			null,
			null

		);
	}

	public static List<PlaceResponseDto> commonPlace(List<CommonPlace> places){
		return places.stream()
			.map(PlaceResponseDto::commonPlace)
			.toList();

	}








}
