package com.adit.backend.domain.event.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.adit.backend.domain.event.converter.UserEventConverter;
import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.event.exception.EventException;
import com.adit.backend.domain.event.repository.UserEventRepository;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.service.command.ImageCommandService;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.query.UserQueryService;
import com.adit.backend.infra.s3.service.AwsS3Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEventCommandService {

	private final UserEventRepository userEventRepository;
	private final UserEventConverter userEventConverter;
	private final UserQueryService userQueryService;
	private final CommonEventCommandService commonEventCommandService;
	private final ImageCommandService imageCommandService;
	private final AwsS3Service awsS3Service;

	public EventResponseDto createUserEvent(EventRequestDto request, Long userId) {
		User user = userQueryService.findUserById(userId);
		CommonEvent commonEvent = commonEventCommandService.saveOrFindCommonEvent(request);
		UserEvent userEvent = userEventConverter.toEntity(request);
		saveUserEventRelation(commonEvent, userEvent, user);
		if (!request.imageUrlList().isEmpty()) {
			imageCommandService.addImageToUserEvent(request, user, userEvent);
		}
		return userEventConverter.toResponse(userEvent);
	}


	public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request, List<MultipartFile> newImageList) {
		UserEvent userEvent = userEventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));

		// 기존 이미지가 있고, 새 이미지가 전달된 경우 업데이트 진행
		if (newImageList != null && !newImageList.isEmpty() && !userEvent.getImages().isEmpty()) {
			List<Image> existingImages = userEvent.getImages();

			// 기존 이미지 개수와 새 이미지 개수 비교 후 업데이트
			for (int i = 0; i < Math.min(existingImages.size(), newImageList.size()); i++) {
				Image oldImage = existingImages.get(i);
				String newImageUrl = awsS3Service.updateImage(oldImage.getUrl(), newImageList.get(i)).join();
				oldImage.updateUrl(newImageUrl);
			}

			// 만약 새 이미지가 더 많다면, 추가로 업로드
			if (newImageList.size() > existingImages.size()) {
				List<Image> extraImages = awsS3Service.uploadFile(
					newImageList.subList(existingImages.size(), newImageList.size()), "event"
				).join();
				extraImages.forEach(userEvent::addImage);
			}
		}

		userEventConverter.updateEntity(userEvent, request);
		UserEvent updatedUserEvent = userEventRepository.save(userEvent);

		return userEventConverter.toResponse(updatedUserEvent);
	}


	private void saveUserEventRelation(CommonEvent commonEvent, UserEvent userEvent, User user) {
		commonEvent.addUserEvent(userEvent);
		user.addUserEvent(userEvent);
		userEventRepository.save(userEvent);
	}
}