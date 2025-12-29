package com.accenture.pokemon.dto;

import com.accenture.pokemon.model.Pokemon;

import java.util.List;

public record PokemonPairResponse(
        List<Pokemon> pokemons
) {
}
