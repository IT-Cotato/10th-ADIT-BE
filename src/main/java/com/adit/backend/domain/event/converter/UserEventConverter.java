package com.adit.backend.domain.event.converter;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.UserEvent;

@Component
public class UserEventConverter {

	public UserEvent toEntity(EventRequestDto request) {
		return UserEvent.createEvent(
			request.name(),
			request.category(),
			request.startDate(),
			request.endDate(),
			request.memo(),
			false
		);
	}

	public EventResponseDto toResponse(UserEvent userEvent) {
		return EventResponseDto.builder()
			.id(userEvent.getId())
			.name(userEvent.getName())
			.category(userEvent.getCategory())
			.startDate(userEvent.getStartDate())
			.endDate(userEvent.getEndDate())
			.memo(userEvent.getMemo())
			.visited(userEvent.getVisited())
			.build();
	}

	public void updateEntity(UserEvent userEvent, EventUpdateRequestDto updateRequest) {
		userEvent.updateEvent(updateRequest);  // Event 엔터티의 update 메서드 호출
	}
}