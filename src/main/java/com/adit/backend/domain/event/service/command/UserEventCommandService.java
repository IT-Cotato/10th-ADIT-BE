package com.adit.backend.domain.event.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.event.converter.UserEventConverter;
import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.event.exception.EventException;
import com.adit.backend.domain.event.repository.UserEventRepository;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.repository.ImageRepository;
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
	private final AwsS3Service s3Service;
	private final UserQueryService userQueryService;
	private final CommonEventCommandService commonEventCommandService;
	private final ImageRepository imageRepository;

	public EventResponseDto createUserEvent(EventRequestDto request, Long userId) {
		User user = userQueryService.findUserById(userId);
		Image image = s3Service.uploadFile(request.imageUrlList(), user).get(0);
		CommonEvent commonEvent = commonEventCommandService.saveOrFindCommonEvent(request, image);
		UserEvent userEvent = userEventConverter.toEntity(request);
		commonEvent.addUserEvent(userEvent);
		user.addUserEvent(userEvent);
		UserEvent savedUserEvent = userEventRepository.save(userEvent);

		Image userEventImage = Image.builder().url(image.getUrl()).build();
		userEvent.addImage(userEventImage);
		imageRepository.save(userEventImage);
		return userEventConverter.toResponse(savedUserEvent);
	}

	public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request) {
		UserEvent userEvent = userEventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));
		userEventConverter.updateEntity(userEvent, request);
		UserEvent updatedUserEvent = userEventRepository.save(userEvent);
		return userEventConverter.toResponse(updatedUserEvent);
	}
}