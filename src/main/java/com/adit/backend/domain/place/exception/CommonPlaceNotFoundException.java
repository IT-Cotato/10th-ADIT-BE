package com.adit.backend.domain.place.exception;

public class CommonPlaceNotFoundException extends RuntimeException {
	public CommonPlaceNotFoundException(String message) {
		super(message);
	}

	public CommonPlaceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
