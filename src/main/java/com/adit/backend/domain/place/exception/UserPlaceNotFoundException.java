package com.adit.backend.domain.place.exception;

public class UserPlaceNotFoundException extends RuntimeException {
	public UserPlaceNotFoundException(String message) {
		super(message);
	}

	public UserPlaceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
