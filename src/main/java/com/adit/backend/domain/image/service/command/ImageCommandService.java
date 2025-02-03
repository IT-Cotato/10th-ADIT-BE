package com.adit.backend.domain.image.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.event.repository.UserEventRepository;
import com.adit.backend.domain.image.dto.request.ImageRequestDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.exception.ImageException;
import com.adit.backend.domain.image.repository.ImageRepository;
import com.adit.backend.domain.place.dto.request.PlaceRequestDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.error.exception.BusinessException;
import com.adit.backend.infra.s3.service.AwsS3Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageCommandService {
	private final CommonPlaceRepository commonPlaceRepository;
	private final ImageRepository imageRepository;
	private final UserEventRepository userEventRepository;
	private final AwsS3Service s3Service;

	public Image uploadImage(ImageRequestDto requestDto) {
		CommonPlace place = commonPlaceRepository.findById(requestDto.commonPlace().getId())
			.orElseThrow(() -> new BusinessException("Place not found", NOT_FOUND_ERROR));

		UserEvent userEvent =
			requestDto.userEvent().getId() != null ? userEventRepository.findById(requestDto.userEvent().getId())
				.orElseThrow(() -> new BusinessException("Event not found", NOT_FOUND_ERROR)) : null;

		Image image = Image.builder()
			.commonPlace(place)
			.userEvent(userEvent)
			.url(requestDto.url())
			.build();

		return imageRepository.save(image);
	}

	// 이미지 삭제
	public void deleteImage(Long imageId) {
		if (!imageRepository.existsById(imageId)) {
			throw new ImageException(IMAGE_NOT_FOUND);
		}
		imageRepository.deleteById(imageId);
	}

	// UserPlace에 이미지 연관관계 추가 후 저장
	public void addImageToUserPlace(PlaceRequestDto request, User user, UserPlace userPlace) {
		Image userPlaceImage = s3Service.uploadFile(request.imageUrlList(), user).get(0);
		userPlace.addImage(userPlaceImage);
		imageRepository.save(userPlaceImage);
	}

	// CommonPlace에 이미지 연관관계 추가 후 저장
	public void addImageToCommonPlace(PlaceRequestDto request, User user, CommonPlace commonPlace) {
		Image commonPlaceImage = s3Service.uploadFile(request.imageUrlList(), user).get(0);
		commonPlace.addImage(commonPlaceImage);
		imageRepository.save(commonPlaceImage);
	}

	// CommonEvent에 이미지 연관관계 추가 후 저장
	public void addImageToCommonEvent(EventRequestDto request, User user, CommonEvent commonEvent) {
		Image image = s3Service.uploadFile(request.imageUrlList(), user).get(0);
		commonEvent.addImage(image);
		imageRepository.save(image);
	}

	// UserEvent에 이미지 연관관계 추가 후 저장
	public void addImageToUserEvent(EventRequestDto request, User user, UserEvent userEvent) {
		Image image = s3Service.uploadFile(request.imageUrlList(), user).get(0);
		userEvent.addImage(image);
		imageRepository.save(image);
	}

}
