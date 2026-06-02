package com.virtulab.platform.session.service;

public class ForbiddenSessionAccessException extends RuntimeException {
    public ForbiddenSessionAccessException() {
        super("Forbidden: session belongs to another user");
    }
}
