package com.accenture.pokemon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemons")
@Getter
@Setter
@NoArgsConstructor
public class PokemonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PokemonTypeEntity> types = new ArrayList<>();

    public PokemonEntity(String name) {
        this.name = name;
    }

    public void addType(String type) {
        PokemonTypeEntity typeEntity = new PokemonTypeEntity(type);
        types.add(typeEntity);
        typeEntity.setPokemon(this);
    }
}
