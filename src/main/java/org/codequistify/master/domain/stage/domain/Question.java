package org.codequistify.master.domain.stage.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString(exclude = "stage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "question",
        indexes = {
                @Index(name = "idx_stageid_questionindex", columnList = "stage_id, question_index")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_stageid_questionindex", columnNames = {"stage_id", "question_index"})
        })
public class Question extends BaseTimeEntity {
    @Id
    @Column(name = "question_id")
    private String id;

    @Column(name = "question_index")
    @NotNull
    private Integer index;

    @Column(name = "title")
    @NotNull
    private String title;

    @Column(name = "description")
    @NotNull
    private String description;

    @Column(name = "answer_type") @ColumnDefault("SHORT_ANSWER")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AnswerType answerType = AnswerType.SHORT_ANSWER;

    @Column(name = "correct_answer")
    @NotNull
    private String correctAnswer = "";

    @Column(name = "is_composable") @ColumnDefault("false")
    @NotNull
    private boolean isComposable = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    @NotNull
    @JsonBackReference
    private Stage stage;

    public List<String> getOptions() {
        return this.options;
    }

    public void setQuestionId(String id) {
        this.id = id;
    }

    public void addStage(Stage stage) {
        this.stage = stage;
    }
    public void setIndex(int index) {
        this.index = index;
    }

}
