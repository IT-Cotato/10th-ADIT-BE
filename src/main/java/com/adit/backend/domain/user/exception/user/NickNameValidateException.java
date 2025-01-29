package com.adit.backend.domain.user.exception.user;

public class NickNameValidateException extends RuntimeException{
	public NickNameValidateException(String message) {
		super(message);
	}

	public NickNameValidateException(String message, Throwable cause) {
		super(message, cause);
	}
}
