package com.adit.backend.domain.event.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.List;
import java.util.stream.IntStream;

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
import com.adit.backend.domain.image.enums.Directory;
import com.adit.backend.domain.image.service.command.ImageCommandService;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEventCommandService {

	private final UserEventRepository userEventRepository;
	private final UserEventConverter userEventConverter;
	private final UserQueryService userQueryService;
	private final CommonEventCommandService commonEventCommandService;
	private final ImageCommandService imageCommandService;

	public EventResponseDto createUserEvent(EventRequestDto request, Long userId) {
		User user = userQueryService.findUserById(userId);
		CommonEvent commonEvent = commonEventCommandService.saveOrFindCommonEvent(request);
		UserEvent userEvent = userEventConverter.toEntity(request);
		saveUserEventRelation(commonEvent, userEvent, user);

		if (request.imageUrlList() != null && !request.imageUrlList().isEmpty()) {
			imageCommandService.addImageToUserEvent(request, user, userEvent);
		}
		return userEventConverter.toResponse(userEvent);
	}

	private void saveUserEventRelation(CommonEvent commonEvent, UserEvent userEvent, User user) {
		commonEvent.addUserEvent(userEvent);
		user.addUserEvent(userEvent);
		userEventRepository.save(userEvent);
	}

	/**
	 * 이벤트 업데이트
	 * 기존 이미지를 S3에서 업데이트하고, 새로운 이미지는 추가하는 방식
	 */
	public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request, List<MultipartFile> newImageList) {
		UserEvent userEvent = userEventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));

		List<Image> existingImages = userEvent.getImages();

		// newImageList가 없거나 비어있다면 이미지 업데이트 생략
		if (newImageList == null || newImageList.isEmpty()) {
			userEventConverter.updateEntity(userEvent, request);
			userEventRepository.save(userEvent);
			return userEventConverter.toResponse(userEvent);
		}

		// 기존 이미지 업데이트 (stream() 활용)
		List<String> updatedImageUrls = IntStream.range(0, Math.min(existingImages.size(), newImageList.size()))
			.mapToObj(i -> {
				Image oldImage = existingImages.get(i);
				return imageCommandService.updateImage(oldImage.getId(), newImageList.get(i)).url();
			})
			.toList(); // 변경된 URL 리스트 저장

		// 업데이트된 URL을 한 번에 반영
		IntStream.range(0, updatedImageUrls.size()).forEach(i -> existingImages.get(i).updateUrl(updatedImageUrls.get(i)));

		// 새로운 이미지 추가 (Directory.EVENT.getPath() 사용)
		if (newImageList.size() > existingImages.size()) {
			List<MultipartFile> extraFiles = newImageList.subList(existingImages.size(), newImageList.size());

			if (!extraFiles.isEmpty()) {
				List<Image> extraImages = imageCommandService.uploadImages(extraFiles, Directory.EVENT.getPath());
				extraImages.forEach(userEvent::addImage);
			}
		}

		userEventConverter.updateEntity(userEvent, request);
		userEventRepository.save(userEvent);

		return userEventConverter.toResponse(userEvent);
	}

	/**
	 * 이벤트 삭제 (관련 이미지도 함께 삭제)
	 */
	public void deleteEvent(Long id) {
		UserEvent userEvent = userEventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));

		// 이미지 삭제 (삭제 실패시 로깅만 수행)
		userEvent.getImages().forEach(image -> {
			try {
				imageCommandService.deleteImage(image.getId());
			} catch (Exception e) {
				log.error("[S3] 이미지 삭제 실패: {}", image.getUrl(), e);
			}
		});

		// 이벤트 삭제
		userEventRepository.delete(userEvent);
	}
}

