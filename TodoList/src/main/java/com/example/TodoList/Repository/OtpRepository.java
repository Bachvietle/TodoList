package com.example.TodoList.Repository;

import com.example.TodoList.Entity.VerifyLoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<VerifyLoginOtp, String> {
    public Optional<VerifyLoginOtp> findByOtp(String otp);
}
