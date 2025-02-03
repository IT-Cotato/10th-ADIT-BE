package com.adit.backend.domain.event.service.query;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.adit.backend.domain.event.converter.EventConverter;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.UserEvent;
import com.adit.backend.domain.event.exception.EventException;
import com.adit.backend.domain.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventQueryService {

	private final EventRepository eventRepository;
	private final EventConverter eventConverter;

	public List<EventResponseDto> getAllEvents() {
		return eventRepository.findAll()
			.stream()
			.map(eventConverter::toResponse)
			.toList();
	}

	public EventResponseDto getEventById(Long id) {
		UserEvent userEvent = eventRepository.findById(id)
			.orElseThrow(() -> new EventException(EVENT_NOT_FOUND));
		return eventConverter.toResponse(userEvent);
	}

	public List<EventResponseDto> getEventsByDate(LocalDate date) {
		return eventRepository.findByDate(date)
			.stream()
			.map(eventConverter::toResponse)
			.toList();
	}

	public List<EventResponseDto> getTodayEvents() {
		LocalDate today = LocalDate.now();
		return getEventsByDate(today);
	}

	public List<EventResponseDto> getNoDateEvents() {
		return eventRepository.findNoDateEvents()
			.stream()
			.map(eventConverter::toResponse)
			.toList();
	}

	public List<EventResponseDto> getPopularEvents() {
		return eventRepository.findPopularEvents()
			.stream()
			.map(eventConverter::toResponse)
			.toList();
	}
}