package com.accenture.pokemon.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DistinctPokemonNamesValidator.class)
@Documented
public @interface DistinctPokemonNames {
    String message() default "Pokemon names must be distinct";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
