package com.adit.backend.domain.event.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.event.converter.CommonEventConverter;
import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.repository.CommonEventRepository;
import com.adit.backend.domain.image.service.command.ImageCommandService;

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
	private final ImageCommandService imageCommandService;

	public CommonEvent saveOrFindCommonEvent(EventRequestDto request) {
		return commonEventRepository.findByName(request.name()).orElseGet(() -> {
			CommonEvent commonEvent = commonEventConverter.toEntity(request);
			imageCommandService.addImageToCommonEvent(request, commonEvent);
			return commonEventRepository.save(commonEvent);
		});
	}
}
