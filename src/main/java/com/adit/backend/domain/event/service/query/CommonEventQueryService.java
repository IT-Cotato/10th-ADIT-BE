package com.adit.backend.domain.event.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.event.entity.CommonEvent;
import com.adit.backend.domain.event.exception.EventException;
import com.adit.backend.domain.event.repository.CommonEventRepository;
import com.adit.backend.global.error.GlobalErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonEventQueryService {

	private final CommonEventRepository commonEventRepository;

	public CommonEvent getEventByName(String eventName) {
		return commonEventRepository.findByName(eventName)
			.orElseThrow(() -> new EventException(GlobalErrorCode.COMMON_EVENT_NOT_FOUND));
	}
}
