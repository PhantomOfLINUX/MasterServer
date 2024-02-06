package org.codequistify.master.domain.stage.domain;

import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "stage")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevelType difficultyLevel;

    @Column(name = "question_count")
    @ColumnDefault("0")
    private Integer questionCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "stage_id")
    private List<Question> questions = new ArrayList<>();

    @PostPersist
    protected void onPostPersist() {
        this.questions = questions.stream()
                .peek(question -> {
                    String id = this.id +
                            "-" +
                            String.format("%02d", question.getIndex()) +
                            "-" +
                            question.getAnswerType().getCode();
                    question.setId(id);
                }).collect(Collectors.toList());
    }
}
