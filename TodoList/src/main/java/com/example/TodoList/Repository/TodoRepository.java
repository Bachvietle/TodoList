package com.example.TodoList.Repository;

import com.example.TodoList.Entity.Todo;
import com.example.TodoList.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TodoRepository extends JpaRepository<Todo, Long> {

    Optional<Todo> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);

    // Tự động linmit, offset
    Page<Todo> findAllByUser(Pageable pageable, User user);
}
