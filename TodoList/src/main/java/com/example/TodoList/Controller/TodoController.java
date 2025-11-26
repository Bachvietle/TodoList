package com.example.TodoList.Controller;

import com.example.TodoList.DTO.Request.TodoDTO;
import com.example.TodoList.DTO.Response.ApiResponse;
import com.example.TodoList.DTO.Response.TodoPageResponse;
import com.example.TodoList.DTO.Response.TodoResponse;
import com.example.TodoList.Entity.Todo;
import com.example.TodoList.Repository.TodoRepository;
import com.example.TodoList.Service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@Transactional
@RequestMapping("/todos")
public class TodoController {
    public final TodoService todoService;

    public final TodoRepository todoRepository;
    @PostMapping
    public ResponseEntity<ApiResponse<TodoResponse>> createTodoItem(@RequestBody TodoDTO todoDTO){

        Todo newTodoItem = todoService.createItem(todoDTO);

        TodoResponse todoResponse = TodoResponse.createTodoResponse(newTodoItem); // trả về TodoResponse để ẩn ttin User

        ApiResponse<TodoResponse> response = ApiResponse.success("Tạo mới thành công", todoResponse);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodoItem(@PathVariable Long id, @RequestBody TodoDTO todoDTO){

        Todo todoItem = todoService.updateItem(id, todoDTO);

        TodoResponse todoResponse = TodoResponse.createTodoResponse(todoItem);

        ApiResponse<TodoResponse> response = ApiResponse.success("Cập nhật thành công", todoResponse);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodoItem(@PathVariable Long id){
         todoService.deleteItem(id);

         return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // lấy List có số Item = limit rồi đưa vào data trả về List, page là offset (page 1 = offset 10, page 2 = offset 20)
    @GetMapping
    public ResponseEntity<ApiResponse<TodoPageResponse>> getAllTodoItem(@RequestParam int page, @RequestParam int limit){
        TodoPageResponse todoPageResponse = todoService.getTodos(limit, page);

        ApiResponse<TodoPageResponse> response = ApiResponse.success("Lấy thành công", todoPageResponse);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
