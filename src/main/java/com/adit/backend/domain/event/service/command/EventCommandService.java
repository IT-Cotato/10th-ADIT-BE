package com.adit.backend.domain.event.service.command;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import org.springframework.stereotype.Service;

import com.adit.backend.domain.event.converter.EventConverter;
import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.Event;
import com.adit.backend.domain.event.exception.EventException;
import com.adit.backend.domain.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventCommandService {

	private final EventRepository eventRepository;
	private final EventConverter eventConverter;

	public EventResponseDto createEvent(EventRequestDto request) {
		Event event = eventConverter.toEntity(request);
		Event savedEvent = eventRepository.save(event);
		return eventConverter.toResponse(savedEvent);
	}

	public EventResponseDto updateEvent(Long id, EventUpdateRequestDto request) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));
		eventConverter.updateEntity(event, request);
		Event updatedEvent = eventRepository.save(event);
		return eventConverter.toResponse(updatedEvent);
	}
}