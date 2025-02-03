package com.adit.backend.domain.place.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.repository.ImageRepository;
import com.adit.backend.domain.place.converter.CommonPlaceConverter;
import com.adit.backend.domain.place.dto.request.CommonPlaceRequestDto;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.exception.PlaceException;
import com.adit.backend.domain.place.repository.UserPlaceRepository;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.domain.user.service.query.UserQueryService;
import com.adit.backend.global.error.exception.BusinessException;
import com.adit.backend.infra.s3.service.AwsS3Service;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPlaceCommandService {

	private final UserRepository userRepository;
	private final UserPlaceRepository userPlaceRepository;
	private final ImageRepository imageRepository;
	private final CommonPlaceConverter commonPlaceConverter;
	private final UserQueryService userQueryService;
	private final CommonPlaceCommandService commonPlaceCommandService;
	private final AwsS3Service s3Service;

	public PlaceResponseDto createUserPlace(Long userId, CommonPlaceRequestDto request) {
		User user = userQueryService.findUserById(userId);
		Image image = s3Service.uploadFile(request.imageUrlList(), user).get(0);
		CommonPlace commonPlace = commonPlaceCommandService.saveOrFindCommonPlace(request, image);
		UserPlace userPlace = UserPlace.builder()
			.user(user)
			.memo(request.memo())
			.visited(false)
			.build();
		commonPlace.addUserPlace(userPlace);
		userPlaceRepository.save(userPlace);

		Image userPlaceImage = Image.builder().url(image.getUrl()).build();
		userPlace.addImage(userPlaceImage);
		imageRepository.save(userPlaceImage);
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
}
