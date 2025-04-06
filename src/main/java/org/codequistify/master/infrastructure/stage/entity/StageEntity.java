package org.codequistify.master.infrastructure.stage.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.codequistify.master.core.domain.stage.model.DifficultyLevelType;
import org.codequistify.master.core.domain.stage.model.StageGroupType;
import org.codequistify.master.core.domain.stage.model.StageImageType;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"questions", "completedStages"})
@Table(name = "stage")
public class StageEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Long id;

    @NotNull
    @Column(name = "title")
    private String title;

    @NotNull
    @Column(name = "description")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "stage_group")
    private StageGroupType stageGroup;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevelType difficultyLevel;

    @Column(name = "count")
    @ColumnDefault("0")
    private Integer questionCount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "stage_image")
    private StageImageType stageImage;

    @Column(name = "approved")
    @ColumnDefault("false")
    private Boolean approved;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<QuestionEntity> questions = new ArrayList<>();

    @OneToMany(mappedBy = "stage", fetch = FetchType.LAZY)
    private Set<CompletedStageEntity> completedStages = new HashSet<>();

    @Builder
    public StageEntity(String title,
                       String description,
                       StageGroupType stageGroup,
                       DifficultyLevelType difficultyLevel,
                       Integer questionCount,
                       StageImageType stageImage,
                       Boolean approved,
                       List<QuestionEntity> questions) {
        this.title = title;
        this.description = description;
        this.stageGroup = stageGroup;
        this.difficultyLevel = difficultyLevel;
        this.questionCount = questionCount != null ? questionCount : 0;
        this.stageImage = stageImage;
        this.approved = approved != null ? approved : false;
        this.questions = new ArrayList<>();
        if (questions != null) {
            questions.forEach(this::addQuestion);
        }
    }

    public void addQuestion(QuestionEntity question) {
        this.questions.add(question);
        question.assignStage(this); // 명시적 양방향 연관 설정
    }

    public void updateStageImage(StageImageType imageType) {
        this.stageImage = imageType;
    }
}
