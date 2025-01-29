package com.adit.backend.domain.place.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.converter.PlaceConverter;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.exception.PlaceException;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommonPlaceQueryService {

	private final CommonPlaceRepository commonPlaceRepository;
	private final PlaceConverter placeConverter;

	private static final int DEFAULT_PAGE_NUMBER = 0;
	private static final int DEFAULT_PAGE_SIZE = 5;

	//인기 기반으로 장소 찾기
	@Transactional(readOnly = true)
	public List<PlaceResponseDto> getPlaceByPopular() {
		//PlaceStatistics 엔티티에서 1위부터 5위까지의 commonplaceId를 가져옴
		Pageable pageable = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
		List<Long> commonPlacesId = commonPlaceRepository.findByPopular(pageable);
		List<CommonPlace> commonPlaces = commonPlacesId.stream()
			.map(id -> commonPlaceRepository.findById(id)
				.orElseThrow(() -> new PlaceException(COMMON_PLACE_NOT_FOUND))
			).toList();

		return commonPlaces.stream().map(placeConverter::commonPlaceToResponse).toList();

	}

	//특정 장소 상세정보 찾기
	@Transactional(readOnly = true)
	public PlaceResponseDto getDetailedPlace(String placeName) {
		if (placeName.isBlank()) {
			throw new BusinessException(NOT_VALID_ERROR);
		}
		CommonPlace commonPlace = commonPlaceRepository.findByBusinessName(placeName)
			.orElseThrow(() -> new PlaceException(COMMON_PLACE_NOT_FOUND));
		return placeConverter.commonPlaceToResponse(commonPlace);

	}
}
