package com.example.TodoList.DTO.Request;

import com.example.TodoList.Entity.TodoStatus;
import lombok.Data;

@Data
public class TodoDTO {
    private String title;

    private String description;

    private TodoStatus status;
}
