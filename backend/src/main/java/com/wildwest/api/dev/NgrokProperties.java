package com.wildwest.api.dev;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wildwest.dev.ngrok")
public record NgrokProperties(
        boolean enabled,
        String binary,
        int port,
        String url
) {}