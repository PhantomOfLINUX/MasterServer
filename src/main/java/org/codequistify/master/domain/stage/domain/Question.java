package org.codequistify.master.domain.stage.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.global.util.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    public void setId(String id) {
        this.id = id;
    }
    public void setIndex(int index) {
        this.index = index;
    }

}
