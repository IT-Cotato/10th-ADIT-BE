package com.adit.backend.domain.place.converter;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.place.dto.request.PlaceRequestDto;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.UserPlace;

@Component
public class UserPlaceConverter {

	public UserPlace toEntity(PlaceRequestDto request) {
		return UserPlace.builder()
			.memo(request.memo())
			.visited(false)
			.build();
	}

	/**
	 * 일반 사용자용 응답 변환 메서드
	 */
	public PlaceResponseDto toResponse(UserPlace userPlace) {
		return PlaceResponseDto.builder()
			.commonPlaceId(userPlace.getCommonPlace().getId())
			.userPlaceId(userPlace.getId())
			.memo(userPlace.getMemo())
			.visited(userPlace.getVisited())
			.placeName(userPlace.getCommonPlace().getPlaceName())
			// CommonPlace 에서 latitude, longitude, url 등 필요한 필드가 존재한다고 가정
			.latitude(userPlace.getCommonPlace().getLatitude())
			.longitude(userPlace.getCommonPlace().getLongitude())
			.subCategory(userPlace.getCommonPlace().getSubCategory())
			.roadAddressName(userPlace.getCommonPlace().getRoadAddressName())
			.addressName(userPlace.getCommonPlace().getAddressName())
			.url(userPlace.getCommonPlace().getUrl())
			// CommonPlace에 연관된 모든 이미지의 url 목록 생성
			.imageUrlList(Optional.ofNullable(userPlace.getImages())
				.orElse(Collections.emptyList())
				.stream()
				.map(Image::getUrl)
				.collect(Collectors.toList()))
			.build();
	}

	/**
	 * 친구용 응답 변환 메서드 (friendUserId, profile 정보 추가)
	 */
	public PlaceResponseDto toFriendResponse(UserPlace userPlace) {
		return PlaceResponseDto.builder()
			.commonPlaceId(userPlace.getCommonPlace().getId())
			.userPlaceId(userPlace.getId())
			.memo(userPlace.getMemo())
			.visited(userPlace.getVisited())
			.placeName(userPlace.getCommonPlace().getPlaceName())
			.latitude(userPlace.getCommonPlace().getLatitude())
			.longitude(userPlace.getCommonPlace().getLongitude())
			.subCategory(userPlace.getCommonPlace().getSubCategory())
			.roadAddressName(userPlace.getCommonPlace().getRoadAddressName())
			.addressName(userPlace.getCommonPlace().getAddressName())
			.url(userPlace.getCommonPlace().getUrl())
			.imageUrlList(userPlace.getCommonPlace().getImages().stream()
				.map(Image::getUrl)
				.collect(Collectors.toList()))
			.friendUserId(userPlace.getUser().getId())
			.profile(userPlace.getUser().getProfile())
			.build();
	}
}
