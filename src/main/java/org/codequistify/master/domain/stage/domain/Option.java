package org.codequistify.master.domain.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.codequistify.master.global.util.BaseTimeEntity;

@Entity
@Table(name = "option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Option extends BaseTimeEntity {
    @Id
    @Column(name = "option_id")
    private String id;

    @Column(name = "text")
    private String text;
}
