package com.accenture.pokemon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<PokemonTypeEntity> types = new HashSet<>();

    public PokemonEntity(String name) {
        this.name = name;
    }

    public List<PokemonTypeEntity> getTypes() {
        return List.copyOf(types);
    }

    public void addType(String type) {
        PokemonTypeEntity typeEntity = new PokemonTypeEntity(type);
        types.add(typeEntity);
        typeEntity.setPokemon(this);
    }
}
