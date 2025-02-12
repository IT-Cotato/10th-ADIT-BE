package com.adit.backend.domain.place.converter;

import java.util.Collections;
import java.util.Optional;
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
		return PlaceResponseDto.builder()
			.commonPlaceId(commonPlace.getId())
			.placeName(commonPlace.getPlaceName())
			.latitude(commonPlace.getLatitude())
			.longitude(commonPlace.getLongitude())
			.subCategory(commonPlace.getSubCategory())
			.roadAddressName(commonPlace.getRoadAddressName())
			.addressName(commonPlace.getAddressName())
			.url(commonPlace.getUrl())
			.imageUrlList(Optional.ofNullable(commonPlace.getImages())
				.orElse(Collections.emptyList())
				.stream()
				.map(Image::getUrl)
				.collect(Collectors.toList()))
			.build();
	}

	public PlaceResponseDto userPlaceToResponse(UserPlace userPlace) {
		return PlaceResponseDto.builder()
			.commonPlaceId(userPlace.getCommonPlace().getId())
			.userPlaceId(userPlace.getId())
			.memo(userPlace.getMemo())
			.visited(userPlace.getVisited())
			.placeName(userPlace.getCommonPlace().getPlaceName())
			.subCategory(userPlace.getCommonPlace().getSubCategory())
			.roadAddressName(userPlace.getCommonPlace().getRoadAddressName())
			.addressName(userPlace.getCommonPlace().getAddressName())
			.imageUrlList(Optional.ofNullable(userPlace.getImages())
				.orElse(Collections.emptyList())
				.stream()
				.map(Image::getUrl)
				.collect(Collectors.toList()))
			.longitude(userPlace.getCommonPlace().getLongitude())
			.latitude(userPlace.getCommonPlace().getLatitude())
			.build();
	}

	public PlaceResponseDto friendToResponse(UserPlace userPlace) {
		return PlaceResponseDto.builder()
			.commonPlaceId(userPlace.getCommonPlace().getId())
			.userPlaceId(userPlace.getId())
			.memo(userPlace.getMemo())
			.visited(userPlace.getVisited())
			.placeName(userPlace.getCommonPlace().getPlaceName())
			.subCategory(userPlace.getCommonPlace().getSubCategory())
			.roadAddressName(userPlace.getCommonPlace().getRoadAddressName())
			.addressName(userPlace.getCommonPlace().getAddressName())
			.imageUrlList(Optional.ofNullable(userPlace.getImages())
				.orElse(Collections.emptyList())
				.stream()
				.map(Image::getUrl)
				.collect(Collectors.toList()))
			.friendUserId(userPlace.getUser().getId())
			.profile(userPlace.getUser().getProfile())
			.build();
	}

}
