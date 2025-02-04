package com.adit.backend.domain.image.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.exception.ImageException;
import com.adit.backend.domain.image.repository.ImageRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageQueryService {
	private final ImageRepository imageRepository;

	// 이미지 조회
	public Image getImageById(Long imageId) {
		return imageRepository.findById(imageId)
			.orElseThrow(() -> new ImageException(IMAGE_NOT_FOUND));
	}
}
