package com.adit.backend.domain.user.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class UserException extends BusinessException {
	public UserException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
