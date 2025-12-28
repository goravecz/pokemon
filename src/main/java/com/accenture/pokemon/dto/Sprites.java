package com.accenture.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing sprite URLs in PokeAPI response.
 * We use front_default as the primary image URL.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Sprites(
        @JsonProperty("front_default")
        String frontDefault
) {
}
