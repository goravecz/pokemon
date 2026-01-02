package com.accenture.pokemon.dto;

import com.accenture.pokemon.validation.DistinctPokemonNames;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BattleRequest(
        @Size(min = 2, max = 2, message = "Exactly two pokemons required")
        @DistinctPokemonNames(message = "Cannot battle the same Pokemon against itself")
        @Schema(
                description = "The two pokemons participating in the battle",
                example = "[\"pikachu\", \"charmander\"]"
        )
        List<@NotBlank(message = "Pokemon name must not be blank")
             @Size(max = 100, message = "Pokemon name too long")
             @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid Pokemon name. Only letters, numbers, and hyphens are allowed") String> pokemons
) {}
