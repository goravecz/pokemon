package com.accenture.pokemon.config;

public record TimeoutConfig(
        int connectMs,
        int readMs
) {
}
