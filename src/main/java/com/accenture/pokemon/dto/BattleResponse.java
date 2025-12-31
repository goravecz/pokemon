package com.accenture.pokemon.dto;

import com.accenture.pokemon.model.Pokemon;

public record BattleResponse(
        Pokemon winner
) {
}
