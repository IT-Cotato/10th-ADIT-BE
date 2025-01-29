package com.adit.backend.domain.place.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.converter.PlaceConverter;
import com.adit.backend.domain.place.dto.request.CommonPlaceRequestDto;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.exception.PlaceException;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.global.error.GlobalErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommonPlaceCommandService {

	private final CommonPlaceRepository commonPlaceRepository;
	private final PlaceConverter placeConverter;

	// 새로운 장소 생성
	public PlaceResponseDto createCommonPlace(CommonPlaceRequestDto requestDto) {
		CommonPlace commonPlace = placeConverter.toEntity(requestDto);
		CommonPlace savePlace = commonPlaceRepository.save(commonPlace);

		// DB에 저장하고 반환
		return placeConverter.commonPlaceToResponse(savePlace);
	}

	public PlaceResponseDto updatePlace(Long placeId, CommonPlaceRequestDto requestDto) {
		CommonPlace place = commonPlaceRepository.findById(placeId)
			.orElseThrow(() -> new PlaceException(GlobalErrorCode.COMMON_PLACE_NOT_FOUND));
		place.updatePlace(
			requestDto.placeName(),
			requestDto.latitude(),
			requestDto.longitude(),
			requestDto.addressName(),
			requestDto.roadAddressName(),
			requestDto.subCategory(),
			requestDto.url()
		);
		return placeConverter.commonPlaceToResponse(place);
	}

}
