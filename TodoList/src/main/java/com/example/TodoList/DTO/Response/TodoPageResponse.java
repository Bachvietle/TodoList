package com.example.TodoList.DTO.Response;

import com.example.TodoList.Entity.Todo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Data
public class TodoPageResponse {
    List<TodoResponse> data;
    int page;
    int limit;
    int total;

    public TodoPageResponse(List<Todo> data, int page, int limit, int total) {

        // Biến List<Todo> -> List<TodoResponse>
        List<TodoResponse> todoResponseList = new ArrayList<>();

        for(int i = 0; i < data.size(); i++){
            todoResponseList.add(TodoResponse.createTodoResponse(data.get(i)));
        }

        this.data = todoResponseList;
        this.page = page;
        this.limit = limit;
        this.total = total;
    }
}
