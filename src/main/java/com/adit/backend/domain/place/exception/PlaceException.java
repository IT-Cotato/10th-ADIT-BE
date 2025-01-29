package com.adit.backend.domain.place.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class PlaceException extends BusinessException {
	public PlaceException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
