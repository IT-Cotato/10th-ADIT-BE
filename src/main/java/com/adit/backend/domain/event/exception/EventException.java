package com.adit.backend.domain.event.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class EventException extends BusinessException {
	public EventException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
