package com.adit.backend.domain.user.exception.user;

public class NickNameNullException extends RuntimeException {
	public NickNameNullException(String message) {
		super(message);
	}

	public NickNameNullException(String message, Throwable cause) {
		super(message, cause);
	}
}
