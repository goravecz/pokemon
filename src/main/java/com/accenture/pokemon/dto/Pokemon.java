package com.accenture.pokemon.dto;

import java.util.List;

public record Pokemon(
        String name,
        List<String> types,
        Integer strength
) {
}
