package com.helpdesk.api.core.exceptions;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
