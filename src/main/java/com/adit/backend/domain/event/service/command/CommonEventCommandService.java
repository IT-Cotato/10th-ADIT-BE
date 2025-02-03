package com.adit.backend.domain.event.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.event.converter.CommonEventConverter;
import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.repository.CommonEventRepository;
import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.image.repository.ImageRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonEventCommandService {
	private final CommonEventRepository commonEventRepository;
	private final CommonEventConverter commonEventConverter;
	private final ImageRepository imageRepository;

	public CommonEvent saveOrFindCommonEvent(EventRequestDto request, Image image) {
		return commonEventRepository.findByName(request.name()).orElseGet(() -> {
			CommonEvent commonEvent = commonEventConverter.toEntity(request);
			imageRepository.save(image);
			commonEvent.addImage(image);
			return commonEventRepository.save(commonEvent);
		});
	}
}
