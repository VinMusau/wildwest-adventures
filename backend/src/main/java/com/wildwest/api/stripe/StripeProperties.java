package com.wildwest.api.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wildwest.stripe")
public record StripeProperties(String secretKey, String webhookSecret) {}