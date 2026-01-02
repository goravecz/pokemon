package com.accenture.pokemon.repository;

import com.accenture.pokemon.entity.BattleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BattleHistoryRepository extends JpaRepository<BattleEntity, Long> {
    List<BattleEntity> findAllByOrderByCreatedAtDesc();
}
