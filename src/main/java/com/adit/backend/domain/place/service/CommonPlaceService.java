package com.adit.backend.domain.place.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.dto.request.CommonPlaceRequestDto;
import com.adit.backend.domain.place.dto.response.CommonPlaceResponseDto;
import com.adit.backend.domain.place.dto.response.UserPlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.domain.place.repository.UserPlaceRepository;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional
public class CommonPlaceService {

	private final CommonPlaceRepository commonPlaceRepository;
	private final UserPlaceRepository userPlaceRepository;

	// 새로운 장소 생성
	public CommonPlace createPlace(CommonPlaceRequestDto requestDto) {
		CommonPlace place = CommonPlace.builder()
			.placeName(requestDto.placeName())
			.addressName(requestDto.addressName())
			.latitude(requestDto.latitude())
			.longitude(requestDto.longitude())
			.roadAddressName(requestDto.roadAddressName())
			.subCategory(requestDto.subCategory())
			.url(requestDto.url())
			.build();

		// DB에 저장하고 반환
		return commonPlaceRepository.save(place);
	}

	// 장소 ID로 조회
	public CommonPlace getPlaceById(Long placeId) {
		return commonPlaceRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
	}

	// 장소 정보 업데이트
	public CommonPlace updatePlace(Long placeId, CommonPlaceRequestDto requestDto) {
		CommonPlace place = getPlaceById(placeId);
		place.updatePlace(
			requestDto.placeName(),
			requestDto.latitude(),
			requestDto.longitude(),
			requestDto.addressName(),
			requestDto.roadAddressName(),
			requestDto.subCategory(),
			requestDto.url()
		);
		return place;
	}

	// 장소 삭제
	public void deletePlace(Long placeId) {
		if (!commonPlaceRepository.existsById(placeId)) {
			throw new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR);
		}
		commonPlaceRepository.deleteById(placeId);
	}

	//카테고리 기반으로 장소 찾기
	public List<UserPlaceResponseDto> getPlaceByCategory(String subCategory, Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByCategory(subCategory, userId)
			   .orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		return  UserPlaceResponseDto.from(userPlaces);

	}

	//인기 기반으로 장소 찾기
	public List<CommonPlaceResponseDto> getPlaceByPopular() {
		Pageable pageable = PageRequest.of(0,5);
		List<Long> commonPlacesId = commonPlaceRepository.findByPopular(pageable)
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		List<CommonPlace> commonPlaces = commonPlacesId.stream()
										.map(id -> commonPlaceRepository.findById(id)
											.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR))
										)
										.toList();

		return CommonPlaceResponseDto.from(commonPlaces);

	}

	public List<UserPlaceResponseDto> getSavedPlace(Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId)
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		return UserPlaceResponseDto.from(userPlaces);
	}

	public CommonPlaceResponseDto getDetailedPlace(String businessName) {
		CommonPlace commonPlace = commonPlaceRepository.findByBusinessName(businessName)
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		return CommonPlaceResponseDto.from(commonPlace);

	}
}
