package com.api.sisventas.models.dtos.auth;

public record AuthResponse(String accessToken, String tokenType, UserResponse user) {

    private static final String BEARER = "Bearer";

    public static AuthResponse of(String accessToken, UserResponse user) {
        return new AuthResponse(accessToken, BEARER, user);
    }
}
