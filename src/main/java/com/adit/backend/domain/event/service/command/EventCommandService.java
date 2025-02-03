package com.adit.backend.domain.event.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;

import com.adit.backend.domain.event.converter.EventConverter;
import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.event.exception.EventException;
import com.adit.backend.domain.event.repository.EventRepository;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.infra.s3.service.AwsS3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventCommandService {

	private final EventRepository eventRepository;
	private final EventConverter eventConverter;
	private final AwsS3Service s3Service;

	public EventResponseDto createEvent(EventRequestDto request, User user) {
		UserEvent userEvent = eventConverter.toEntity(request);
		UserEvent savedUserEvent = eventRepository.save(userEvent);
		return eventConverter.toResponse(savedUserEvent);
	}

	public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request) {
		UserEvent userEvent = eventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));
		eventConverter.updateEntity(userEvent, request);
		UserEvent updatedUserEvent = eventRepository.save(userEvent);
		return eventConverter.toResponse(updatedUserEvent);
	}
}