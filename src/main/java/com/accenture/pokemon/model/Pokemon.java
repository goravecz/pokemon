package com.accenture.pokemon.model;

import java.util.List;

public record Pokemon(
        String name,
        List<String> types,
        String imageUrl,
        Integer strength
) {
}
