package org.codequistify.master.yumin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Getter
public class Task {
    @Id
    private String id;

    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "start_date", updatable = false, columnDefinition = "DATETIME(0)")
    private Date startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "end_date", updatable = false, columnDefinition = "DATETIME(0)")
    private Date endDate;

    private Boolean isDone;

    public Task(String id, String description, Date startDate, Date endDate, Boolean isDone) {
        this.id = id;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isDone = isDone;
    }

    public Task() {

    }
}
