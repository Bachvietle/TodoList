package com.example.TodoList.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Table(name = "register_token")
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor

public class VerifyRegisterToken {

    @Id
    private String token; // UUID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

}
