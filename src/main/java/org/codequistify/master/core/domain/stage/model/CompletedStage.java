package org.codequistify.master.core.domain.stage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.core.domain.player.model.Player;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
public class CompletedStage {

    private final LocalDateTime createdAt;
    private final Long id;
    private final Player player;
    private final Integer questionIndex;
    private final Stage stage;
    private final CompletedStatus status;
    private final LocalDateTime updatedAt;

    public CompletedStage markCompleted() {
        return this.status == CompletedStatus.IN_PROGRESS
                ? this.toBuilder().status(CompletedStatus.COMPLETED).build()
                : this;
    }

    public CompletedStage withQuestionIndex(int index) {
        return this.toBuilder().questionIndex(index).build();
    }
}
