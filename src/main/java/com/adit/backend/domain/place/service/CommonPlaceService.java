package com.adit.backend.domain.place.service;

import java.util.List;

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

	//저장된 장소 찾기
	public List<UserPlaceResponseDto> getSavedPlace(Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId)
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		return UserPlaceResponseDto.from(userPlaces);
	}

	//특정 장소 상세정보 찾기
	public CommonPlaceResponseDto getDetailedPlace(String businessName) {
		CommonPlace commonPlace = commonPlaceRepository.findByBusinessName(businessName)
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		return CommonPlaceResponseDto.from(commonPlace);

	}

	//현재 위치 기반 장소 찾기
	public List<UserPlaceResponseDto> getPlaceByLocation(double userLatitude, double userLongitude, Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId)
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		if (userPlaces.size() == 1){
			return UserPlaceResponseDto.from(userPlaces);
		}
		// 저장한 장소가 2개 이상일때 정렬
		List<UserPlace> placeByLocation = userPlaces.stream().sorted((place1, place2) -> {
			double distance1 = getDistance(place1.getCommonPlace().getLatitude().doubleValue(),place1.getCommonPlace().getLongitude().doubleValue(), userLatitude, userLongitude);
			double distance2 = getDistance(place2.getCommonPlace().getLatitude().doubleValue(),place2.getCommonPlace().getLongitude().doubleValue(), userLatitude, userLongitude);
			return Double.compare(distance1,distance2);
		})
			.toList();
		return UserPlaceResponseDto.from(placeByLocation);
	}

	//거리 계산 메소드
	public double getDistance(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat/2)* Math.sin(dLat/2)+ Math.cos(Math.toRadians(lat1))* Math.cos(Math.toRadians(lat2))* Math.sin(dLon/2)* Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return R * c;
	}
}
