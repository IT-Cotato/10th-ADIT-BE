package com.adit.backend.domain.place.converter;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.dto.request.PlaceRequestDto;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;

@Component
public class CommonPlaceConverter {
	public CommonPlace toEntity(PlaceRequestDto requestDto, Long placeId) {
		return CommonPlace.builder()
			.id(placeId)
			.placeName(requestDto.placeName())
			.addressName(requestDto.addressName())
			.latitude(requestDto.latitude())
			.longitude(requestDto.longitude())
			.roadAddressName(requestDto.roadAddressName())
			.subCategory(requestDto.subCategory())
			.url(requestDto.url())
			.build();

	}

	public PlaceResponseDto commonPlaceToResponse(CommonPlace commonPlace) {
		return new PlaceResponseDto(
			commonPlace.getId(),
			null,
			null,
			null,
			commonPlace.getPlaceName(),
			commonPlace.getLatitude(),
			commonPlace.getLongitude(),
			commonPlace.getSubCategory(),
			commonPlace.getRoadAddressName(),
			commonPlace.getAddressName(),
			commonPlace.getUrl(),
			commonPlace.getImages().stream().map(Image::getUrl).collect(Collectors.toList()),
			null,
			null

		);
	}

	public PlaceResponseDto userPlaceToResponse(UserPlace userPlace) {
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
			userPlace.getCommonPlace().getImages().stream().map(Image::getUrl).toList(),
			null,
			null
		);
	}

	public PlaceResponseDto friendToResponse(UserPlace userPlace) {
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

}
