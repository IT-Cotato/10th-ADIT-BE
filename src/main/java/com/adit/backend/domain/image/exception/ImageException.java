package com.adit.backend.domain.image.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class ImageException extends BusinessException {
	public ImageException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
