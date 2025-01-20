package com.adit.backend.domain.place.exception;

public class FriendNotFoundException extends RuntimeException {
	public FriendNotFoundException(String message) {
		super(message);
	}

	public FriendNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
