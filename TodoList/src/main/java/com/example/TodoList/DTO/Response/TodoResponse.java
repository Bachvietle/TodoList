package com.example.TodoList.DTO.Response;

import com.example.TodoList.Entity.Todo;
import com.example.TodoList.Entity.TodoStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoResponse {

    private Long id;

    private String title;

    private String description;

    private TodoStatus status;

    private Long userID;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public TodoResponse(Long id, String title, String description, TodoStatus status, Long userID, LocalDateTime createAt, LocalDateTime updateAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.userID = userID;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public static TodoResponse createTodoResponse(Todo todo){
        return new TodoResponse(todo.getId(), todo.getTitle(), todo.getDescription(), todo.getStatus(), todo.getUser().getId(), todo.getCreateAt(), todo.getCreateAt());
    }
}
