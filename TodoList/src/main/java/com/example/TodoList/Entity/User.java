package com.example.TodoList.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/*
  `user_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `user_name` varchar(30) NOT NULL,
  `user_password` varchar(255) NOT NULL,
  `avatar_url` varchar(512) DEFAULT NULL,
  `role` enum('admin','user') DEFAULT 'user',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `email_verified` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at
 */

@Entity
@Data
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotEmpty
    private String email;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoleType role = RoleType.user;

    @Builder.Default
    private int enabled = 1;

    @Column(name = "email_verified")
    @Builder.Default
    private int emailVerified = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createAt;

    @Column(name = "updated_at")
    @CreationTimestamp
    private LocalDateTime updateAt;


    public String getUserName() {
        return this.userName;
    }

    // Override method from UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Trả về role dưới dạng "ROLE_admin" hoặc "ROLE_user"
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.email; // Trong Spring Security, getUsername() chỉ trả về một string unique để identify user
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Mặc định true; customize nếu cần thêm field
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Mặc định true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Mặc định true
    }

    @Override
    public boolean isEnabled() {
        return this.enabled == 1;  // Dựa trên field enabled
    }
}
