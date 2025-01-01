package com.adit.backend.domain.ai.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class AiException extends BusinessException {
	public AiException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
