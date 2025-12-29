package com.accenture.pokemon.mapper;

import com.accenture.pokemon.dto.PokeApiResponse;
import com.accenture.pokemon.model.Pokemon;
import org.springframework.stereotype.Component;

@Component
public class PokemonMapper {

    public Pokemon toPokemon(PokeApiResponse response, int strength) {
        return new Pokemon(
                response.name(),
                response.getTypeNames(),
                response.sprites().frontDefault(),
                strength
        );
    }
}
