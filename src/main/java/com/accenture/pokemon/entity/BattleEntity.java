package com.accenture.pokemon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "battles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BattleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BattleParticipantEntity> participants = new ArrayList<>();

    @Column(nullable = false)
    private String winnerName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BattleEntity() {
    }

    public void addParticipant(BattleParticipantEntity participant) {
        participants.add(participant);
        participant.setBattle(this);
    }
}
