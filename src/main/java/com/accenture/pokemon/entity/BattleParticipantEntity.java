package com.accenture.pokemon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "battle_participants")
@Getter
@Setter
@NoArgsConstructor
public class BattleParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private BattleEntity battle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private PokemonEntity pokemon;

    @Column(nullable = false)
    private Integer strength;

    public BattleParticipantEntity(PokemonEntity pokemon, Integer strength) {
        this.pokemon = pokemon;
        this.strength = strength;
    }
}
