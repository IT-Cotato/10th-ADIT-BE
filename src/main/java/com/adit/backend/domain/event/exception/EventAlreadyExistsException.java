package com.adit.backend.domain.event.exception;

public class EventAlreadyExistsException extends RuntimeException {
    public EventAlreadyExistsException(String message) {
        super(message);
    }

    public EventAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
