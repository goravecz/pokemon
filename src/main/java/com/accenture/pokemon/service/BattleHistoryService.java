package com.accenture.pokemon.service;

import com.accenture.pokemon.dto.BattleHistoryResponse;
import com.accenture.pokemon.entity.BattleEntity;
import com.accenture.pokemon.mapper.BattleMapper;
import com.accenture.pokemon.repository.BattleHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BattleHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(BattleHistoryService.class);

    private final BattleHistoryRepository battleHistoryRepository;
    private final BattleMapper battleMapper;

    public BattleHistoryService(BattleHistoryRepository battleHistoryRepository, BattleMapper battleMapper) {
        this.battleHistoryRepository = battleHistoryRepository;
        this.battleMapper = battleMapper;
    }

    public BattleHistoryResponse getBattleHistory() {
        List<BattleEntity> battleEntities = battleHistoryRepository.findAllByOrderByCreatedAtDesc();
        
        List<com.accenture.pokemon.dto.Battle> battles = battleEntities.stream()
                .map(battleMapper::toBattleDto)
                .toList();
        
        LOG.info("Retrieved {} battles from history", battles.size());
        return new BattleHistoryResponse(battles);
    }
}
