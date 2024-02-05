package org.codequistify.master.domain.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.codequistify.master.global.util.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseTimeEntity {
    @Id
    @Column(name = "question_id")
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "question_id")
    private List<Option> options = new ArrayList<>();


}
