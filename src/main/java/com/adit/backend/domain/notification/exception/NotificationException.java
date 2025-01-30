package com.adit.backend.domain.notification.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class NotificationException extends BusinessException {
	public NotificationException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
