package com.helpdesk.api.core.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class NoResourceFoundException extends EntityNotFoundException {
  public NoResourceFoundException(String message) {
    super(message);
  }
}
