package com.adit.backend.domain.event.exception;

public class EventDeletionFailedException extends RuntimeException {
    public EventDeletionFailedException(String message) {
        super(message);
    }

    public EventDeletionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
