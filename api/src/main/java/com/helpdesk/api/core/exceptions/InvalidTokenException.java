package com.helpdesk.api.core.exceptions;

/** Mapped to 400 by {@link GlobalExceptionHandler} (subclass of IllegalArgumentException). */
public class InvalidTokenException extends IllegalArgumentException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
