package com.accenture.pokemon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<BattleParticipantEntity> participants = new HashSet<>();

    @Column(nullable = false)
    private String winnerName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BattleEntity() {
    }

    public List<BattleParticipantEntity> getParticipants() {
        return List.copyOf(participants);
    }

    public void addParticipant(BattleParticipantEntity participant) {
        participants.add(participant);
        participant.setBattle(this);
    }
}
