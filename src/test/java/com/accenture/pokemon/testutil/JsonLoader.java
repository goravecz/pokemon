package com.accenture.pokemon.testutil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class JsonLoader {

    private JsonLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final String MOCK_RESPONSES_PATH = "src/test/resources/mock-responses/";

    public static String loadJson(String pokemonName) {
        try {
            return Files.readString(
                    Paths.get(MOCK_RESPONSES_PATH + pokemonName.toLowerCase() + ".json"),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mock JSON for: " + pokemonName, e);
        }
    }

    public static String getPikachuJson() {
        return loadJson("pikachu");
    }

    public static String getCharizardJson() {
        return loadJson("charizard");
    }

    public static String getBulbasaurJson() {
        return loadJson("bulbasaur");
    }

    public static String getSquirtleJson() {
        return loadJson("squirtle");
    }
}
