package com.accenture.pokemon.model;

import java.util.List;

public record Battle(
        List<Pokemon> pokemons,
        Pokemon winner) {

    public Battle {
        if (pokemons == null) {
            throw new IllegalArgumentException("Pokemons list cannot be null");
        }
        if (pokemons.size() != 2) {
            throw new IllegalArgumentException("Battle must have exactly two pokemons");
        }
        if (winner == null) {
            throw new IllegalArgumentException("Winner cannot be null");
        }
        if (!pokemons.contains(winner)) {
            throw new IllegalArgumentException("Winner must be one of the battle participants");
        }
        pokemons = List.copyOf(pokemons);
    }
}
