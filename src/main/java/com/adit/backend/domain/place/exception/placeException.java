package com.adit.backend.domain.place.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class placeException extends BusinessException {
	public placeException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
