package com.accenture.pokemon.repository;

import com.accenture.pokemon.entity.PokemonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<PokemonEntity, Long> {
    Optional<PokemonEntity> findByName(String name);
}
