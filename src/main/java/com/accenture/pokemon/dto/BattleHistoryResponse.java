package com.accenture.pokemon.dto;

import java.util.List;

public record BattleHistoryResponse(
        List<Battle> battles
) {
}
