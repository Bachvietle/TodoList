package com.example.TodoList.Service;

import com.example.TodoList.DTO.Request.TodoDTO;
import com.example.TodoList.DTO.Response.TodoPageResponse;
import com.example.TodoList.Entity.Todo;
import com.example.TodoList.Entity.User;
import com.example.TodoList.Exception.TodoException;
import com.example.TodoList.Repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

/*
- SecurityContextHolder là một class static của Spring Security, dùng để lưu trữ SecurityContext
- Mỗi Thread có 1 bản copy SecurityContext riêng, chứa Auth của user, ko share giữa Threads
 */

/*
- Mỗi request của user sẽ đc xử lý bởi 1 thread
- Request -> Thread 1 validate Jwt, set Auth vào SecurityContext
               -> Xử lý request -> Response -> Clear auth -> Thread quay về pool
 */

/*
- khi gửi request, Auth user đã được lưu ở SecurityContext (sau khi đi qua Filter)
- Vì vậy ta chỉ cần getContext().getAuthentication();
 */
public class TodoService {

    final TodoRepository todoRepository;

    // 1. Create
    public Todo createItem(TodoDTO todoDTO){

        // Lấy User của Thread hiện tại (Sau khi qua JwtAuthFilter, authUser đc lưu trong SecurityContextHolder)
        Authentication authUser = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authUser.getPrincipal();

        // Tạo newTodoItem
        Todo newTodoItem = Todo.builder()
                .title(todoDTO.getTitle())
                .description(todoDTO.getDescription())
                .user(user)
                .build();

        todoRepository.save(newTodoItem);

        return newTodoItem;
    }

    // 2. Update
    public Todo updateItem(Long id, TodoDTO todoDTO){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) auth.getPrincipal();

        // Jpa cần 1 no-arg constructor để load entity từ DB vào
        Todo todoItem = todoRepository.findByIdAndUser(id, user).orElseThrow(
                () -> new TodoException("Ko tìm thấy ghi chú")
        );

        todoItem.setTitle(todoDTO.getTitle());
        todoItem.setDescription(todoDTO.getDescription());
        todoItem.setStatus(todoDTO.getStatus());

        todoRepository.save(todoItem);

        return todoItem;
    }

    // 3. Delete
    public void deleteItem(Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) auth.getPrincipal();

        todoRepository.findByIdAndUser(id, user).orElseThrow(
                () -> new TodoException("Ko tìm thấy ghi chú")
        );

        todoRepository.deleteByIdAndUser(id, user);
    }

    // 4. Get
    public TodoPageResponse getTodos(int limit, int page){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) auth.getPrincipal();

        // interface Pageable lưu trữ các yêu vầu phân trang,
        // khi dùng trong repo(câu lệnh ở dưới) JPA sẽ tự limit và offset theo pageable
        Pageable pageable = PageRequest.of(page - 1, limit); // index trang bắt đầu từ 0

        // Page<T> là KQ trả về của 1 truy vấn trang
        Page<Todo> todoPage = todoRepository.findAllByUser(pageable, user);
        /*
         Bao gồm các ttin:
         - getContent() -> dữ liệu của trang hiện tại List<T>
         - getTotalElements() -> tổng số toàn bộ record
         - getTotalPages() -> tổng số page có thể
         - getNumber() -> trang hiện tại (từ 0)
         - getSize() -> số phần tử 1 trang
         - hasNext() / hasPrevious() → Có trang kế tiếp/trước không
         */

        return new TodoPageResponse(
                todoPage.getContent(),
                todoPage.getNumber() + 1,
                todoPage.getSize(),
                (int) todoPage.getTotalElements()
        );
    }
}
