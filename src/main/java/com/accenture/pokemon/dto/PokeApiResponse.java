package com.accenture.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO for mapping PokeAPI response JSON.
 * Example response structure:
 * {
 *   "name": "pikachu",
 *   "types": [{"slot":1,"type":{"name":"electric","url":"..."}}],
 *   "sprites": {"front_default": "https://..."}
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiResponse(
        String name,
        List<TypeSlot> types,
        Sprites sprites
) {
    public List<String> getTypeNames() {
        return types.stream()
                .map(TypeSlot::type)
                .map(TypeDetail::name)
                .toList();
    }
}
