package com.accenture.pokemon.controller;

import com.accenture.pokemon.dto.PokemonPairResponse;
import com.accenture.pokemon.service.PokemonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PokemonController {

    private static final Logger LOG = LoggerFactory.getLogger(PokemonController.class);

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @Operation(summary = "Request a random Pokemon pair")
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
}
