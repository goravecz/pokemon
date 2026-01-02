package com.accenture.pokemon.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistinctPokemonNamesValidator implements ConstraintValidator<DistinctPokemonNames, List<String>> {

    @Override
    public boolean isValid(List<String> pokemonNames, ConstraintValidatorContext context) {
        if (pokemonNames == null || pokemonNames.isEmpty()) {
            return true; // Let @Size handle empty list validation
        }

        Set<String> uniqueNames = new HashSet<>();
        for (String name : pokemonNames) {
            if (name != null && !uniqueNames.add(name.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
