package org.codequistify.master.infrastructure.stage.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.core.domain.stage.model.CompletedStatus;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.codequistify.master.infrastructure.player.entity.PlayerEntity;

@Getter
@ToString(exclude = "stage")
@Entity
@Table(name = "completed_stage")
public class CompletedStageEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completed_stage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    @JsonBackReference
    private StageEntity stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompletedStatus status;

    @Column(name = "question_index", nullable = false)
    private Integer questionIndex = 0;

    protected CompletedStageEntity() {
    }

    @Builder
    public CompletedStageEntity(PlayerEntity player, StageEntity stage, CompletedStatus status) {
        this.player = player;
        this.stage = stage;
        this.status = status;
        this.questionIndex = 0;
    }
}
