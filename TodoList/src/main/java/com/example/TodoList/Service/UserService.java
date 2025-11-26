package com.example.TodoList.Service;

import com.example.TodoList.DTO.Request.UserRegisterDTO;
import com.example.TodoList.Entity.User;
import com.example.TodoList.Entity.VerifyRegisterToken;
import com.example.TodoList.Exception.EmailExistsException;
import com.example.TodoList.Repository.UserRepository;
import com.example.TodoList.Repository.TokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    public void registerUser(UserRegisterDTO userRegisterDTO)  throws MessagingException {

        if(userRepository.existsByEmail(userRegisterDTO.getEmail())){
            throw new EmailExistsException("Email đã được đăng kí");
        }

        User user = User.builder()
                .email(userRegisterDTO.getEmail())
                .userName(userRegisterDTO.getUserName())
                .userPassword(passwordEncoder.encode(userRegisterDTO.getUserPassword()))
                .enabled(0)
                .build();

        userRepository.save(user);

        // Tạo VerifyRegisterToken gửi về email dưới dạng link
        String token = UUID.randomUUID().toString();

        VerifyRegisterToken verifyRegisterToken = VerifyRegisterToken.builder()
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .user(user)
                .build();

        tokenRepository.save(verifyRegisterToken);

        String verifyLink = "http://localhost:8080/user/verify_register_token?token=" + token;

        emailService.sendVerifyRegisterMail(verifyLink, userRegisterDTO.getEmail());
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Ko tìm thấy user bằng email")
        );

        return user;
    }
}
