package com.adit.backend.domain.user.exception.friend;

public class FriendRequestNotFoundException extends RuntimeException{
	public FriendRequestNotFoundException(String message) {
		super(message);
	}

	public FriendRequestNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
