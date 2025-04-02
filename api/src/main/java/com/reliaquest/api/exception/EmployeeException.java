package com.reliaquest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmployeeException extends ResponseStatusException {
    public EmployeeException(HttpStatus status, String message) {
        super(status, message);
    }
}
