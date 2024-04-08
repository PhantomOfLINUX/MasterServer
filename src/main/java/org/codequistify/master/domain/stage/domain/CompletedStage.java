package org.codequistify.master.domain.stage.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.global.util.BaseTimeEntity;

@ToString(exclude = "stage")
@Getter
@Entity
@Table(name = "completed_stage")
public class CompletedStage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    @JsonBackReference
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
