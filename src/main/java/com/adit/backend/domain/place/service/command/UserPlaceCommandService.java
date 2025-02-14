package com.adit.backend.domain.place.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.adit.backend.domain.event.entity.EventStatistics;
import com.adit.backend.domain.event.repository.EventStatisticsRepository;
import com.adit.backend.domain.image.converter.ImageConverter;
import com.adit.backend.domain.image.dto.response.ImageResponseDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.exception.ImageException;
import com.adit.backend.domain.image.repository.ImageRepository;
import com.adit.backend.domain.image.service.command.ImageCommandService;
import com.adit.backend.domain.place.converter.CommonPlaceConverter;
import com.adit.backend.domain.place.converter.UserPlaceConverter;
import com.adit.backend.domain.place.dto.request.PlaceRequestDto;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.exception.PlaceException;
import com.adit.backend.domain.place.repository.UserPlaceRepository;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.query.UserQueryService;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPlaceCommandService {

	private final UserPlaceRepository userPlaceRepository;
	private final EventStatisticsRepository eventStatisticsRepository;
	private final CommonPlaceConverter commonPlaceConverter;
	private final UserPlaceConverter userPlaceConverter;
	private final UserQueryService userQueryService;
	private final CommonPlaceCommandService commonPlaceCommandService;
	private final ImageCommandService imageCommandService;
	private final PlaceStatisticsCommandService placeStatisticsCommandService;
	private final ImageRepository imageRepository;
	private final ImageConverter imageConverter;

	// 장소 저장시, EventStatistics.bookmarkCount 증가
	public PlaceResponseDto createUserPlace(Long userId, PlaceRequestDto request) {
		//장소 중복 검사
		if(!duplicatePlace(userId, request)) {
			throw new PlaceException(USER_PLACE_DUPLICATE);
		}
		User user = userQueryService.findUserById(userId);
		CommonPlace commonPlace = commonPlaceCommandService.saveOrFindCommonPlace(request);
		UserPlace userPlace = userPlaceConverter.toEntity(request);
		saveUserPlaceRelation(user, commonPlace, userPlace);

		/**
		 * EventStatistics의 bookmarkCount 증가
 		 */
		eventStatisticsRepository.findByCommonEventId(commonPlace.getId())
			.ifPresent(EventStatistics::incrementBookmarkCount);

		if (!request.imageUrlList().isEmpty()) {
			imageCommandService.addImageToUserPlace(request, user, userPlace);
		}
		placeStatisticsCommandService.saveOrCount(commonPlace);
		return commonPlaceConverter.userPlaceToResponse(userPlace);
	}

	// 장소 삭제
	public void deletePlace(Long userPlaceId) {
		if (!userPlaceRepository.existsById(userPlaceId)) {
			throw new PlaceException(USER_PLACE_NOT_FOUND);
		}
		userPlaceRepository.deleteById(userPlaceId);
	}

	//장소 메모 수정
	public PlaceResponseDto updateUserPlace(Long userPlaceId, String memo) {
		UserPlace place = userPlaceRepository.findById(userPlaceId)
			.orElseThrow(() -> new PlaceException(USER_PLACE_NOT_FOUND));
		if (memo.isBlank()) {
			throw new BusinessException(NOT_VALID_ERROR);
		}
		place.updatedMemo(memo);
		return commonPlaceConverter.userPlaceToResponse(place);
	}

	//장소 방문 여부 표시
	public void checkVisitedPlace(Long userPlaceId) {
		UserPlace place = userPlaceRepository.findById(userPlaceId)
			.orElseThrow(() -> new PlaceException(USER_PLACE_NOT_FOUND));
		place.updatedVisited();
	}

	// user, commonPlace와 UserPlace 사이의 연관관계 설정 및 저장
	private void saveUserPlaceRelation(User user, CommonPlace commonPlace, UserPlace userPlace) {
		user.addUserPlace(userPlace);
		commonPlace.addUserPlace(userPlace);
		userPlaceRepository.save(userPlace);
	}

	public boolean duplicatePlace(Long userId, PlaceRequestDto request) {
		UserPlace userPlace = userPlaceRepository.findDuplicatePlace(userId, request.url());
		return userPlace == null;
	}

	public ImageResponseDto updateUserPlaceImage(Long userPlaceId, MultipartFile multipartFile) {
		Image image = imageRepository.findByUserPlaceId(userPlaceId)
			.orElseThrow(() -> new ImageException(IMAGE_NOT_FOUND));
		ImageResponseDto imageResponseDto = imageCommandService.updateImage(image.getId(), multipartFile);
		return imageConverter.toResponseForUserPlace(imageResponseDto);
	}
}
