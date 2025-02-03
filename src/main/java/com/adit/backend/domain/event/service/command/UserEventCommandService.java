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
import com.adit.backend.domain.image.service.command.ImageCommandService;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.query.UserQueryService;

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

	public EventResponseDto createUserEvent(EventRequestDto request, Long userId) {
		User user = userQueryService.findUserById(userId);
		CommonEvent commonEvent = commonEventCommandService.saveOrFindCommonEvent(request);
		UserEvent userEvent = userEventConverter.toEntity(request);
		saveUserEventRelation(commonEvent, userEvent, user);
		imageCommandService.addImageToUserEvent(request, user, userEvent);
		return userEventConverter.toResponse(userEvent);
	}

	public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request) {
		UserEvent userEvent = userEventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));
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