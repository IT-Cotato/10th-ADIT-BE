package com.adit.backend.domain.user.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class FriendShipException extends BusinessException {
	public FriendShipException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
