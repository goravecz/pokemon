package com.accenture.pokemon.controller;

import com.accenture.pokemon.dto.BattleHistoryResponse;
import com.accenture.pokemon.dto.BattleRequest;
import com.accenture.pokemon.dto.BattleResponse;
import com.accenture.pokemon.dto.PokemonPairResponse;
import com.accenture.pokemon.service.BattleHistoryService;
import com.accenture.pokemon.service.BattleService;
import com.accenture.pokemon.service.PokemonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PokemonController {

    private static final Logger LOG = LoggerFactory.getLogger(PokemonController.class);

    private final PokemonService pokemonService;
    private final BattleService battleService;
    private final BattleHistoryService battleHistoryService;

    public PokemonController(
            PokemonService pokemonService,
            BattleService battleService,
            BattleHistoryService battleHistoryService) {
        this.pokemonService = pokemonService;
        this.battleService = battleService;
        this.battleHistoryService = battleHistoryService;
    }

    @Operation(summary = "Returns a random Pokemon pair")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned two random pokemons"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - Error communicating with external API"),
            @ApiResponse(responseCode = "503", description = "Service Unavailable - External API temporarily unavailable"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/pokemons")
    public ResponseEntity<PokemonPairResponse> getPokemons() {
        LOG.info("Pokemons requested.");
        final PokemonPairResponse pokemons = pokemonService.getPokemons();
        LOG.info("Pokemons returned: {}", pokemons);

        return ResponseEntity.ok().body(pokemons);
    }

    @Operation(summary = "Returns the winner of the battle between the supplied pokemons")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned the result of the battle"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Pokemon(s) not found"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - Error communicating with external API"),
            @ApiResponse(responseCode = "503", description = "Service Unavailable - External API temporarily unavailable"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/battles")
    public ResponseEntity<BattleResponse> getBattleResult(
            @RequestBody(description = "Battle request with two Pokemon names",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"pokemons\": [\"pikachu\", \"charizard\"]}")))
            @Valid @org.springframework.web.bind.annotation.RequestBody BattleRequest request) {
        LOG.info("BattleRequest: {}", request);
        final BattleResponse response = battleService.getBattleResult(request);
        LOG.info("BattleResponse: {}", response);

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Returns battle history with details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned past battles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/battles/history")
    public ResponseEntity<BattleHistoryResponse> getBattleHistory() {
        LOG.info("Battle history requested.");
        final BattleHistoryResponse response = battleHistoryService.getBattleHistory();
        LOG.info("BattleHistoryResponse: {}", response);

        return ResponseEntity.ok().body(response);
    }
}
