package com.adit.backend.global.error.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final GlobalErrorCode errorCode;

	public BusinessException(String message, GlobalErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public BusinessException(GlobalErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public GlobalErrorCode getErrorCode() {
		return errorCode;
	}
}
