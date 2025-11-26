package com.example.TodoList.Repository;

import com.example.TodoList.Entity.VerifyRegisterToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<VerifyRegisterToken, String> {

    public Optional<VerifyRegisterToken> findByToken(String token);
}
