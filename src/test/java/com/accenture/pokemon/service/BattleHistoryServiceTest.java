package com.accenture.pokemon.service;

import com.accenture.pokemon.dto.Battle;
import com.accenture.pokemon.dto.BattleHistoryResponse;
import com.accenture.pokemon.entity.BattleEntity;
import com.accenture.pokemon.entity.BattleParticipantEntity;
import com.accenture.pokemon.entity.PokemonEntity;
import com.accenture.pokemon.mapper.BattleMapper;
import com.accenture.pokemon.repository.BattleHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.accenture.pokemon.testutil.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleHistoryServiceTest {

    @Mock
    private BattleHistoryRepository battleHistoryRepository;

    @Mock
    private BattleMapper battleMapper;

    private BattleHistoryService battleHistoryService;

    @BeforeEach
    void setUp() {
        battleHistoryService = new BattleHistoryService(battleHistoryRepository, battleMapper);
    }

    @Test
    void getBattleHistory_shouldReturnEmptyList_whenNoBattlesExist() {
        // given
        when(battleHistoryRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        // when
        BattleHistoryResponse response = battleHistoryService.getBattleHistory();

        // then
        assertThat(response.battles()).isEmpty();
    }

    @Test
    void getBattleHistory_shouldReturnBattles_whenBattlesExist() {
        // given
        BattleEntity battleEntity = createBattleEntity(PIKACHU_NAME, CHARIZARD_NAME, CHARIZARD_NAME);
        Battle battleDto = new Battle(List.of(), CHARIZARD_NAME);

        when(battleHistoryRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(battleEntity));
        when(battleMapper.toBattleDto(battleEntity)).thenReturn(battleDto);

        // when
        BattleHistoryResponse response = battleHistoryService.getBattleHistory();

        // then
        assertThat(response.battles()).hasSize(1);
        assertThat(response.battles().get(0).winner()).isEqualTo(CHARIZARD_NAME);
    }

    @Test
    void getBattleHistory_shouldReturnMultipleBattles_whenMultipleBattlesExist() {
        // given
        BattleEntity battle1 = createBattleEntity(PIKACHU_NAME, CHARIZARD_NAME, CHARIZARD_NAME);
        BattleEntity battle2 = createBattleEntity(SQUIRTLE_NAME, BULBASAUR_NAME, BULBASAUR_NAME);
        
        Battle battleDto1 = new Battle(List.of(), CHARIZARD_NAME);
        Battle battleDto2 = new Battle(List.of(), BULBASAUR_NAME);

        when(battleHistoryRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(battle1, battle2));
        when(battleMapper.toBattleDto(battle1)).thenReturn(battleDto1);
        when(battleMapper.toBattleDto(battle2)).thenReturn(battleDto2);

        // when
        BattleHistoryResponse response = battleHistoryService.getBattleHistory();

        // then
        assertThat(response.battles()).hasSize(2);
        assertThat(response.battles().get(0).winner()).isEqualTo(CHARIZARD_NAME);
        assertThat(response.battles().get(1).winner()).isEqualTo(BULBASAUR_NAME);
    }

    private BattleEntity createBattleEntity(String pokemon1Name, String pokemon2Name, String winnerName) {
        BattleEntity entity = new BattleEntity();
        entity.setWinnerName(winnerName);

        PokemonEntity pokemon1 = new PokemonEntity(pokemon1Name);
        PokemonEntity pokemon2 = new PokemonEntity(pokemon2Name);

        entity.addParticipant(new BattleParticipantEntity(pokemon1, PIKACHU_STRENGTH));
        entity.addParticipant(new BattleParticipantEntity(pokemon2, CHARIZARD_STRENGTH));

        return entity;
    }
}
