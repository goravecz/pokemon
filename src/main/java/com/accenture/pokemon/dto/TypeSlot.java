package com.accenture.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TypeSlot(
        int slot,
        TypeDetail type
) {
}
