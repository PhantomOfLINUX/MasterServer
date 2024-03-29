package org.codequistify.master.domain.stage.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.global.util.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "question", indexes = @Index(name = "idx_question_id", columnList = "id"))
public class Question extends BaseTimeEntity {
    @Id
    @Column(name = "question_id")
    private String id;

    @Column(name = "question_index")
    private Integer index;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "answer_type")
    @Enumerated(EnumType.STRING)
    private AnswerType answerType;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
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
