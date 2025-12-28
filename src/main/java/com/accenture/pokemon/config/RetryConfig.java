package com.accenture.pokemon.config;

public record RetryConfig(
        int maxAttempts,
        int initialBackoffMs,
        double multiplier,
        long maxInterval
) {
}
