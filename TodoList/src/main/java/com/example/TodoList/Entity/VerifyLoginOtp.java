package com.example.TodoList.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Table(name = "login_otp")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class VerifyLoginOtp {

    @Email
    @Id
    private String email;

    @Column(unique = true)
    private String otp;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // Otp ko cần biết user được tạo hay chưa, chỉ cần email + otp đúng
    // (vì xác minh email có user ở bước trc rồi mới đến gửi otp)
}
