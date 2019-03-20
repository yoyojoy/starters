package com.shengyecapital.boot.process.exception;

public class CustomProcessException extends RuntimeException {

    public CustomProcessException(String message, Throwable t) {
        super(message, t);
    }

    public CustomProcessException(String message) {
        super(message);
    }
}
