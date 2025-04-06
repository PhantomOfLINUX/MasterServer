package org.codequistify.master.infrastructure.stage.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.codequistify.master.core.domain.stage.model.AnswerType;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@ToString(exclude = "stage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "question",
        indexes = @Index(name = "idx_stageid_questionindex", columnList = "stage_id, question_index"),
        uniqueConstraints = @UniqueConstraint(name = "uc_stageid_questionindex", columnNames = {"stage_id", "question_index"})
)
public class QuestionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_id")
    private Long id;

    @NotNull
    @Column(name = "question_index")
    private Integer index;

    @NotNull
    @Column(name = "title")
    private String title;

    @NotNull
    @Column(name = "description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type")
    @ColumnDefault("'SHORT_ANSWER'")
    private AnswerType answerType;

    @NotNull
    @Column(name = "correct_answer")
    private String correctAnswer;

    @NotNull
    @Column(name = "is_composable")
    @ColumnDefault("false")
    private boolean composable;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option")
    private List<String> options = new ArrayList<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    @JsonBackReference
    private StageEntity stage;

    @Builder
    public QuestionEntity(Integer index,
                          String title,
                          String description,
                          AnswerType answerType,
                          String correctAnswer,
                          boolean composable,
                          List<String> options,
                          StageEntity stage) {
        this.index = index;
        this.title = title;
        this.description = description;
        this.answerType = answerType != null ? answerType : AnswerType.SHORT_ANSWER;
        this.correctAnswer = correctAnswer != null ? correctAnswer : "";
        this.composable = composable;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.stage = stage;
    }

    /**
     * 양방향 관계 Stage -> Question 연결
     */
    public void assignStage(StageEntity stage) {
        this.stage = stage;
    }
}
