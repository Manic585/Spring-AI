package com.practice.springAI.exception;

import org.springframework.http.HttpStatus;

public class SpringAiException extends RuntimeException {

    private final HttpStatus status;

    public SpringAiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public SpringAiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
