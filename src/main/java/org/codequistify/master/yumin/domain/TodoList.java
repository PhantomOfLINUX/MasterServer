package org.codequistify.master.yumin.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class TodoList {
    @Id
    String id;

    String author;

    int size;

    public TodoList(String code, String author, int size) {
        this.id = code;
        this.author = author;
        this.size = size;
    }

    public TodoList() {

    }
}
