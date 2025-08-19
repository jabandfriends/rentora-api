package com.rentora.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JwtProperties {

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.access.expiration}")
    private int ACCESS_EXPIRATION;

    @Value("${jwt.refresh.expiration}")
    private long REFRESH_EXPIRATION;
}
