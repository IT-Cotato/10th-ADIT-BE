package com.adit.backend.domain.place.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.converter.CommonPlaceConverter;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.exception.PlaceException;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.domain.place.repository.UserPlaceRepository;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommonPlaceQueryService {

	private final CommonPlaceRepository commonPlaceRepository;
	private final CommonPlaceConverter commonPlaceConverter;
	private final UserPlaceRepository userPlaceRepository;

	private static final int DEFAULT_PAGE_NUMBER = 0;
	private static final int DEFAULT_PAGE_SIZE = 5;

	public CommonPlace getCommonPlaceById(Long id) {
		return commonPlaceRepository.findById(id)
			.orElseThrow(() -> new PlaceException(COMMON_PLACE_NOT_FOUND));
	}

	//인기 기반으로 장소 찾기
	public List<PlaceResponseDto> getPlaceByPopular() {
		//PlaceStatistics 엔티티에서 1위부터 5위까지의 commonplaceId를 가져옴
		Pageable pageable = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
		List<Long> commonPlacesId = commonPlaceRepository.findByPopular(pageable);
		List<CommonPlace> commonPlaces = commonPlacesId.stream()
			.map(this::getCommonPlaceById).toList();
		return commonPlaces.stream().map(commonPlaceConverter::commonPlaceToResponse).toList();
	}

	//특정 장소 상세정보 찾기
	public PlaceResponseDto getDetailedPlace(String placeName) {
		if (placeName.isBlank()) {
			throw new BusinessException(NOT_VALID_ERROR);
		}
		CommonPlace commonPlace = commonPlaceRepository.findByBusinessName(placeName)
			.orElseThrow(() -> new PlaceException(COMMON_PLACE_NOT_FOUND));
		return commonPlaceConverter.commonPlaceToResponse(commonPlace);

	}

	public Map<CommonPlace, Integer> countCommonPlacesByFriends(List<Long> friendsId) {
		Map<CommonPlace, Integer> friendsCommonplace = new HashMap<>();
		friendsId.forEach(id -> {
			userPlaceRepository.findByUserId(id)
				.forEach(userPlace -> friendsCommonplace.merge(userPlace.getCommonPlace(), 1, Integer::sum));
		});
		return friendsCommonplace;
	}

	public List<CommonPlace> sortCommonPlacesByFrequency(Map<CommonPlace, Integer> friendsCommonplace) {
		return friendsCommonplace.keySet().stream()
			.sorted((a1, b1) -> friendsCommonplace.get(b1) - friendsCommonplace.get(a1))
			.toList();
	}


}
