package com.adit.backend.domain.place.service.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.image.service.command.ImageCommandService;
import com.adit.backend.domain.place.converter.CommonPlaceConverter;
import com.adit.backend.domain.place.dto.request.PlaceRequestDto;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.domain.place.service.query.CommonPlaceQueryService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommonPlaceCommandService {

	private final CommonPlaceRepository commonPlaceRepository;
	private final CommonPlaceQueryService commonPlaceQueryService;
	private final ImageCommandService imageCommandService;
	private final CommonPlaceConverter commonPlaceConverter;

	// 카카오맵 url -> 기존 공통 장소 반환 or 새로운 공통 장소 생성
	public CommonPlace saveOrFindCommonPlace(PlaceRequestDto request) {
		Long commonPlaceId = extractTrailingDigits(request.url());
		return commonPlaceRepository.findById(commonPlaceId).orElseGet(() -> {
			CommonPlace commonPlace = commonPlaceConverter.toEntity(request, commonPlaceId);
			if (!request.imageUrlList().isEmpty()) {
				imageCommandService.addImageToCommonPlace(request, commonPlace);
			}
			return commonPlaceRepository.save(commonPlace);
		});
	}

	public PlaceResponseDto updatePlace(Long placeId, PlaceRequestDto requestDto) {
		CommonPlace place = commonPlaceQueryService.getCommonPlaceById(placeId);
		place.updatePlace(requestDto);
		return commonPlaceConverter.commonPlaceToResponse(place);
	}

	public long extractTrailingDigits(String url) {
		Pattern pattern = Pattern.compile("(\\d+)$");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return Long.parseLong(matcher.group(1));
		}
		return 0;
	}
}
