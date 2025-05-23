package org.codequistify.master.core.domain.stage.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.core.domain.player.model.Player;
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

    @Column(name = "question_index", columnDefinition = "0")
    private Integer questionIndex = 0;

    @Builder
    public CompletedStage(Player player, Stage stage, CompletedStatus status) {
        this.player = player;
        this.stage = stage;
        this.status = status;
    }

    protected CompletedStage() {
    }

    public void updateQuestionIndex(Integer questionIndex) {
        this.questionIndex = questionIndex;
    }

    public void updateCompleted() {
        if (!status.equals(CompletedStatus.IN_PROGRESS)) {
            return;
        }
        this.status = CompletedStatus.COMPLETED;
    }
}
