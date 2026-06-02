package com.virtulab.platform.session.web;

import com.virtulab.platform.contracts.common.ApiError;
import com.virtulab.platform.session.service.ForbiddenSessionAccessException;
import com.virtulab.platform.session.service.SessionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(SessionNotFoundException ex) {
        return new ApiError("SESSION_NOT_FOUND", ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(ForbiddenSessionAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(ForbiddenSessionAccessException ex) {
        return new ApiError("FORBIDDEN", ex.getMessage(), Instant.now());
    }
}
