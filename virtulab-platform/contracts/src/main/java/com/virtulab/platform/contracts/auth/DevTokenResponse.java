package com.virtulab.platform.contracts.auth;

public record DevTokenResponse(String token, String tokenType, long expiresInSeconds) {}
