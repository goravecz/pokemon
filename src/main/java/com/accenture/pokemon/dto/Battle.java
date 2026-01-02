package com.accenture.pokemon.dto;

import java.util.List;

public record Battle(
        List<Pokemon> pokemons,
        String winner
) {
}
