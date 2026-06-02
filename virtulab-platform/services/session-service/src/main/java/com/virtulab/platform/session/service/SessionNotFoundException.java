package com.virtulab.platform.session.service;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String attemptId) {
        super("Session not found: " + attemptId);
    }
}
