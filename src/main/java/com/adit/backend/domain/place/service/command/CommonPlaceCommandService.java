package com.adit.backend.domain.place.service.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.repository.ImageRepository;
import com.adit.backend.domain.place.converter.PlaceConverter;
import com.adit.backend.domain.place.dto.request.CommonPlaceRequestDto;
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
	private final PlaceConverter placeConverter;
	private final ImageRepository imageRepository;

	// 새로운 장소 생성
	public CommonPlace saveOrFindCommonPlace(CommonPlaceRequestDto requestDto, Image image) {
		Long commonPlaceId = extractTrailingDigits(requestDto.url());
		return commonPlaceRepository.findById(commonPlaceId).orElseGet(() -> {
			CommonPlace commonPlace = placeConverter.toEntity(requestDto, commonPlaceId);
			imageRepository.save(image);
			commonPlace.addImage(image);
			return commonPlaceRepository.save(commonPlace);
		});
	}

	public PlaceResponseDto updatePlace(Long placeId, CommonPlaceRequestDto requestDto) {
		CommonPlace place = commonPlaceQueryService.getCommonPlaceById(placeId);
		place.updatePlace(requestDto);
		return placeConverter.commonPlaceToResponse(place);
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
