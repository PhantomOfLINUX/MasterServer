package org.codequistify.master.domain.stage.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stage", indexes = @Index(name = "idx_code_idx", columnList = "code"))
public class Stage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Long id;

    @Column(name = "code", unique = true)
    private Long code;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "stage_group")
    @Enumerated(EnumType.STRING)
    private StageGroupType stageGroup;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevelType difficultyLevel;

    @Column(name = "count")
    @ColumnDefault("0")
    private Integer questionCount;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Question> questions = new ArrayList<>();

    @Column(name = "stage_image")
    @Enumerated(EnumType.STRING)
    private StageImageType stageImage;

    @Column(name = "exp")
    private Integer exp;

    @OneToMany(mappedBy = "stage")
    private Set<CompletedStage> completedStages = new HashSet<>();

    public void updateStageImage(StageImageType stageImageType) {
        this.stageImage = stageImageType;
    }

    @PostPersist
    protected void onPostPersist() {
        // question id 할당
        this.questions = questions.stream()
                .peek(question -> {
                    String id = "Q" +
                            String.format("%03d", this.code) +
                            "-" +
                            String.format("%03d", question.getIndex()) +
                            "-" +
                            question.getAnswerType().getCode();
                    question.setQuestionId(id);
                    question.addStage(this);
                }).collect(Collectors.toList());
    }
}
