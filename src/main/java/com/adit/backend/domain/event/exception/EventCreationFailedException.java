package com.adit.backend.domain.event.exception;

public class EventCreationFailedException extends RuntimeException {
    public EventCreationFailedException(String message) {
        super(message);
    }

    public EventCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
