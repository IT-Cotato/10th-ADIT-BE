package com.adit.backend.domain.place.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.dto.request.CommonPlaceRequestDto;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.exception.placeException;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.domain.place.repository.UserPlaceRepository;

import com.adit.backend.domain.user.repository.FriendshipRepository;
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
	private final FriendshipRepository friendshipRepository;

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
	@Transactional(readOnly = true)
	public CommonPlace getPlaceById(Long placeId) {
		return commonPlaceRepository.findById(placeId)
			.orElseThrow(() -> new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR));
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
	public void deletePlace(Long userPlaceId) {
		if (!userPlaceRepository.existsById(userPlaceId)) {
			throw new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR);
		}
		userPlaceRepository.deleteById(userPlaceId);
	}

	//카테고리 기반으로 장소 찾기
	@Transactional(readOnly = true)
	public List<PlaceResponseDto> getPlaceByCategory(String subCategory, Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByCategory(subCategory, userId);
		if (userPlaces.isEmpty()){
			throw new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR);
		}

		return  PlaceResponseDto.userPlace(userPlaces);

	}

	//인기 기반으로 장소 찾기
	@Transactional(readOnly = true)
	public List<PlaceResponseDto> getPlaceByPopular() {
		//PlaceStatistics 엔티티에서 1위부터 5위까지의 commonplaceId를 가져옴
		Pageable pageable = PageRequest.of(0,5);
		List<Long> commonPlacesId = commonPlaceRepository.findByPopular(pageable);
		if (commonPlacesId.isEmpty()){
			throw new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR);
		}
		List<CommonPlace> commonPlaces = commonPlacesId.stream()
										.map(id -> commonPlaceRepository.findById(id)
											.orElseThrow(() -> new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR))
										)
										.toList();

		return PlaceResponseDto.commonPlace(commonPlaces);

	}

	//저장된 장소 찾기
	@Transactional(readOnly = true)
	public List<PlaceResponseDto> getSavedPlace(Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId);
		if (userPlaces.isEmpty()){
			throw new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR);
		}
		return PlaceResponseDto.userPlace(userPlaces);
	}

	//특정 장소 상세정보 찾기
	@Transactional(readOnly = true)
	public PlaceResponseDto getDetailedPlace(String placeName) {
		CommonPlace commonPlace = commonPlaceRepository.findByBusinessName(placeName)
			.orElseThrow(() -> new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR));
		return PlaceResponseDto.commonPlace(commonPlace);

	}

	//현재 위치 기반 장소 찾기
	@Transactional(readOnly = true)
	public List<PlaceResponseDto> getPlaceByLocation(double userLatitude, double userLongitude, Long userId) {
		List<UserPlace> userPlaces = userPlaceRepository.findByUserId(userId);
		if (userPlaces.isEmpty()){
			throw new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR);
		}
		if (userPlaces.size() == 1){
			return PlaceResponseDto.userPlace(userPlaces);
		}
		// 저장한 장소가 2개 이상일때 정렬
		List<UserPlace> placeByLocation = userPlaces.stream().sorted((place1, place2) -> {
			double distance1 = getDistance(place1.getCommonPlace().getLatitude().doubleValue(),place1.getCommonPlace().getLongitude().doubleValue(), userLatitude, userLongitude);
			double distance2 = getDistance(place2.getCommonPlace().getLatitude().doubleValue(),place2.getCommonPlace().getLongitude().doubleValue(), userLatitude, userLongitude);
			return Double.compare(distance1,distance2);
		})
			.toList();
		return PlaceResponseDto.userPlace(placeByLocation);
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

	//주소 기반 장소 찾기
	@Transactional(readOnly = true)
	public List<PlaceResponseDto> getPlaceByAddress(List<String> address, Long userId) {
		//중복 제거를 위한 Set
		Set<UserPlace> userPlaceSet = new HashSet<>();
		address.forEach(partialAddress -> {
			List<UserPlace> foundPlaces = userPlaceRepository.findByAddress(partialAddress, userId);
			userPlaceSet.addAll(foundPlaces);
		});
		List<UserPlace> userPlaces = new ArrayList<>(userPlaceSet);
		if (userPlaces.isEmpty()){
			throw new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR);
		}
		return PlaceResponseDto.userPlace(userPlaces);


	}

	//장소 방문 여부 표시
	public void checkVisitedPlace(Long userPlaceId) {
		UserPlace place = userPlaceRepository.findById(userPlaceId).orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));
		place.updatedVisited();
	}

	//친구 기반 장소 찾기
	public List<PlaceResponseDto> getPlaceByFriend(Long userId) {
		//사용자의 친구 ID 찾기
		List<Long> friendsId = friendshipRepository.findFriends(userId)
			.orElseThrow(() -> new placeException(GlobalErrorCode.ID_NOT_FOUND_ERROR));

		Set<UserPlace> friendsCommonplaceSet = new HashSet<>();
		friendsId.forEach(id -> {
			List<UserPlace> foundPlaces = userPlaceRepository.findByUserId(id);
			friendsCommonplaceSet.addAll(foundPlaces);
		});
		 List<UserPlace> friendsCommonplace = new ArrayList<>(friendsCommonplaceSet);
		 return PlaceResponseDto.friend(friendsCommonplace);
	}

	//장소 메모 수정
	public PlaceResponseDto updateUserPlace(Long userPlaceId, String memo) {
		UserPlace place = userPlaceRepository.findById(userPlaceId).orElseThrow(() -> new placeException(GlobalErrorCode.PLACE_NOT_FOUND_ERROR));
		place.updatedMemo(memo);
		return PlaceResponseDto.userPlace(place);
	}
}
