package com.adit.backend.domain.event.exception;

public class EventUpdateFailedException extends RuntimeException {
    public EventUpdateFailedException(String message) {
        super(message);
    }

    public EventUpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
