package org.codequistify.master.domain.stage.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.domain.player.domain.Player;

@ToString
@Getter
@Entity
@Table(name = "completed_stage")
public class CompletedStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    private Stage stage;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CompletedStatus status;

    @Builder
    public CompletedStage(Player player, Stage stage, CompletedStatus status) {
        this.player = player;
        this.stage = stage;
        this.status = status;
    }

    protected CompletedStage() {
    }
}
