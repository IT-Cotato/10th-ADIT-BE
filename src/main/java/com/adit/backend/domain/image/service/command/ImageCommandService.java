package com.adit.backend.domain.image.service.command;

import static com.adit.backend.domain.image.enums.Directory.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.image.converter.ImageConverter;
import com.adit.backend.domain.image.dto.response.ImageResponseDto;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.repository.ImageRepository;
import com.adit.backend.domain.image.service.query.ImageQueryService;
import com.adit.backend.domain.place.dto.request.PlaceRequestDto;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.place.entity.UserPlace;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.infra.s3.service.AwsS3Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageCommandService {
	private final ImageRepository imageRepository;
	private final ImageConverter imageConverter;
	private final AwsS3Service s3Service;
	private final ImageQueryService imageQueryService;

	public String uploadImage(String url) {
		return s3Service.uploadFile(List.of(url), TEST.getPath()).join().get(0).getUrl();
	}

	public ImageResponseDto updateImage(Long imageId, MultipartFile multipartFile) {
		Image image = imageQueryService.getImageById(imageId);
		String newImageUrl = s3Service.updateImage(image.getUrl(), multipartFile).join();
		image.updateUrl(newImageUrl);
		return imageConverter.toResponse(image);
	}

	// 이미지 삭제
	public void deleteImage(Long imageId) {
		Image image = imageQueryService.getImageById(imageId);
		s3Service.deleteFile(image.getUrl());
		imageRepository.delete(image);
	}

	// UserPlace에 이미지 연관관계 추가 후 저장
	public void addImageToUserPlace(PlaceRequestDto request, User user, UserPlace userPlace) {
		Image userPlaceImage = s3Service.uploadFile(request.imageUrlList(), USER.getPath() + user.getId())
			.join()
			.get(0);
		userPlace.addImage(userPlaceImage);
		imageRepository.save(userPlaceImage);
	}

	// CommonPlace에 이미지 연관관계 추가 후 저장
	public void addImageToCommonPlace(PlaceRequestDto request, CommonPlace commonPlace) {
		Image commonPlaceImage = s3Service.uploadFile(request.imageUrlList(), PLACE.getPath()).join().get(0);
		commonPlace.addImage(commonPlaceImage);
		imageRepository.save(commonPlaceImage);
	}

	// CommonEvent에 이미지 연관관계 추가 후 저장
	public void addImageToCommonEvent(EventRequestDto request, CommonEvent commonEvent) {
		Image image = s3Service.uploadFile(request.imageUrlList(), EVENT.getPath()).join().get(0);
		commonEvent.addImage(image);
		imageRepository.save(image);
	}

	// UserEvent에 이미지 연관관계 추가 후 저장
	public void addImageToUserEvent(EventRequestDto request, User user, UserEvent userEvent) {
		Image image = s3Service.uploadFile(request.imageUrlList(), USER.getPath() + user.getId()).join().get(0);
		userEvent.addImage(image);
		imageRepository.save(image);
	}
}
