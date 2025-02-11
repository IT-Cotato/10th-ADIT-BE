package com.adit.backend.domain.event.converter;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.image.entity.Image;

@Component
public class CommonEventConverter {

	public CommonEvent toEntity(EventRequestDto request) {
		return CommonEvent.createEvent(
			request.name(),
			request.category(),
			request.startDate(),
			request.endDate(),
			request.memo()
		);
	}

	public EventResponseDto toResponse(CommonEvent commonEvent) {
		return EventResponseDto.builder()
			.id(commonEvent.getId())
			.name(commonEvent.getName())
			.category(commonEvent.getCategory())
			.startDate(commonEvent.getStartDate())
			.endDate(commonEvent.getEndDate())
			.memo(commonEvent.getMemo())
			.imageUrlList(Optional.ofNullable(commonEvent.getImages())
				.orElse(Collections.emptyList())
				.stream()
				.map(Image::getUrl)
				.collect(Collectors.toList()))
			.build();
	}

	public void updateEntity(CommonEvent commonEvent, EventUpdateRequestDto updateRequest) {
		commonEvent.updateEvent(updateRequest);  // Event 엔터티의 update 메서드 호출
	}
}