package com.adit.backend.global.security.jwt.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class TokenException extends BusinessException {
	public TokenException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
