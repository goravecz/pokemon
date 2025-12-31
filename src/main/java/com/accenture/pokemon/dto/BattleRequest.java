package com.accenture.pokemon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BattleRequest(
        @Size(min = 2, max = 2, message = "Exactly two pokemons required")
        @Schema(
                description = "The two pokemons participating in the battle",
                example = "[\"pikachu\", \"charmander\"]"
        )
        List<@NotBlank(message = "Pokemon name must not be blank") String> pokemons
) {}
