package com.adit.backend.domain.image.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.event.repository.EventRepository;
import com.adit.backend.domain.image.dto.request.ImageRequestDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.exception.ImageException;
import com.adit.backend.domain.image.repository.ImageRepository;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.repository.CommonPlaceRepository;
import com.adit.backend.global.error.exception.BusinessException;

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
	private final EventRepository eventRepository;

	public Image uploadImage(ImageRequestDto requestDto) {
		CommonPlace place = commonPlaceRepository.findById(requestDto.commonPlace().getId())
			.orElseThrow(() -> new BusinessException("Place not found", NOT_FOUND_ERROR));

		UserEvent userEvent = requestDto.userEvent().getId() != null ? eventRepository.findById(requestDto.userEvent().getId())
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
}
