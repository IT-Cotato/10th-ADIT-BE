package com.adit.backend.domain.place.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.place.converter.PlaceConverter;
import com.adit.backend.domain.place.dto.response.PlaceResponseDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.exception.NotValidException;
import com.adit.backend.domain.place.exception.UserPlaceNotFoundException;
import com.adit.backend.domain.place.repository.UserPlaceRepository;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.exception.UserException;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPlaceCommandService {

	private final UserRepository userRepository;
	private final UserPlaceRepository userPlaceRepository;
	private final PlaceConverter placeConverter;

	public PlaceResponseDto createUserPlace(Long userId, PlaceResponseDto responseDto, String memo) {
		if (memo.isBlank()) {
			throw new BusinessException(GlobalErrorCode.NOT_VALID_ERROR);
		}
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserException(GlobalErrorCode.USER_NOT_FOUND));
		UserPlace userPlace = UserPlace.builder()
			.user(user)
			.memo(memo)
			.visited(false)
			.build();
		CommonPlace commonPlace = placeConverter.toEntity(responseDto);
		commonPlace.addUserPlace(userPlace);
		UserPlace savePlace = userPlaceRepository.save(userPlace);
		return placeConverter.userPlaceToResponse(savePlace);
	}

	// 장소 삭제
	public void deletePlace(Long userPlaceId) {
		if (!userPlaceRepository.existsById(userPlaceId)) {
			throw new UserPlaceNotFoundException("UserPlace not found");
		}
		userPlaceRepository.deleteById(userPlaceId);
	}

	//장소 메모 수정
	public PlaceResponseDto updateUserPlace(Long userPlaceId, String memo) {
		UserPlace place = userPlaceRepository.findById(userPlaceId)
			.orElseThrow(() -> new UserPlaceNotFoundException("UserPlace not found"));
		if (memo.isBlank()) {
			throw new NotValidException("RequestParam not valid");
		}
		place.updatedMemo(memo);
		return placeConverter.userPlaceToResponse(place);
	}

	//장소 방문 여부 표시
	public void checkVisitedPlace(Long userPlaceId) {
		UserPlace place = userPlaceRepository.findById(userPlaceId)
			.orElseThrow(() -> new UserPlaceNotFoundException("UserPlace not found"));
		place.updatedVisited();
	}
}
