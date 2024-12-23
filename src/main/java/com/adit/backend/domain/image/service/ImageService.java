package com.adit.backend.domain.image.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.event.entity.Event;
import com.adit.backend.domain.event.repository.EventRepository;
import com.adit.backend.domain.image.dto.request.ImageRequestDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.repository.ImageRepository;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional
public class ImageService {

	private final ImageRepository imageRepository;
	private final CommonPlaceRepository commonPlaceRepository;
	private final EventRepository eventRepository;

	// 이미지 업로드

	public Image uploadImage(ImageRequestDto requestDto) {
		CommonPlace place = commonPlaceRepository.findById(requestDto.place().getId())
			.orElseThrow(() -> new BusinessException("Place not found", GlobalErrorCode.NOT_FOUND_ERROR));

		Event event = requestDto.event().getId() != null ? eventRepository.findById(requestDto.event().getId())
			.orElseThrow(() -> new BusinessException("Event not found", GlobalErrorCode.NOT_FOUND_ERROR)) : null;

		Image image = Image.builder()
			.place(place)
			.event(event)
			.url(requestDto.url())
			.fileName(requestDto.fileName())
			.folderName(requestDto.folderName())
			.build();

		return imageRepository.save(image);
	}

	// 이미지 조회
	public Image getImageById(Long imageId) {
		return imageRepository.findById(imageId)
			.orElseThrow(() -> new BusinessException("Image not found", GlobalErrorCode.NOT_FOUND_ERROR));
	}

	// 이미지 삭제
	public void deleteImage(Long imageId) {
		if (!imageRepository.existsById(imageId)) {
			throw new BusinessException("Image not found", GlobalErrorCode.NOT_FOUND_ERROR);
		}
		imageRepository.deleteById(imageId);
	}
}
