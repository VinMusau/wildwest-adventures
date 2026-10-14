package com.wildwest.api.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wildwest.jwt")
public record JwtProperties(String secret, long expirationHours) {}