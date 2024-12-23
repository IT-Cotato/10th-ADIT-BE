package com.adit.backend.global.error.exception;

import com.adit.backend.global.error.GlobalErrorCode;

public class TokenException extends BusinessException {
	public TokenException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
