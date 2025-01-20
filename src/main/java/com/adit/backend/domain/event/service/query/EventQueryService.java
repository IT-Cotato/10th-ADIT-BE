package com.adit.backend.domain.event.service.query;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.adit.backend.domain.event.converter.EventConverter;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.Event;
import com.adit.backend.domain.event.repository.EventRepository;

import jakarta.persistence.EntityNotFoundException;
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
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));
        return eventConverter.toResponse(event);
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