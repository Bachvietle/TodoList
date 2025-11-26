package com.example.TodoList.Controller;

import com.example.TodoList.DTO.Request.UserLoginDTO;
import com.example.TodoList.DTO.Request.UserRegisterDTO;
import com.example.TodoList.DTO.Response.ApiResponse;
import com.example.TodoList.Entity.User;
import com.example.TodoList.Entity.VerifyRegisterToken;
import com.example.TodoList.Repository.TokenRepository;
import com.example.TodoList.Repository.UserRepository;
import com.example.TodoList.Service.AuthService;
import com.example.TodoList.Service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    // 1. Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserRegisterDTO userRegisterDTO) throws MessagingException {
        userService.registerUser(userRegisterDTO);

        ApiResponse apiResponse = ApiResponse.success("Đã gửi email verify", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // 2. Xác thực Email (Dùng GET và Redirect về Frontend)
    @GetMapping("/verify_register_token") // Mặc định redirect link trong email là GET request
    public void verifyRegisterToken(@RequestParam String token, HttpServletResponse response) throws IOException {
        try {
            VerifyRegisterToken verifyRegisterToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token ko hợp lệ"));

            // Kiểm tra hết hạn
            if(verifyRegisterToken.getExpiryDate().isBefore(LocalDateTime.now())){
                tokenRepository.delete(verifyRegisterToken);
                // Chuyển hướng về trang Login React kèm báo lỗi
                response.sendRedirect("http://localhost:5173/login?error=token_expired");
                return;
            }

            // Kích hoạt user
            User user = verifyRegisterToken.getUser();
            user.setEmailVerified(1);
            user.setEnabled(1);
            userRepository.save(user);

            // Xóa token đã dùng
            tokenRepository.delete(verifyRegisterToken);

            // Chuyển hướng về trang Login React kèm báo thành công
            response.sendRedirect("http://localhost:5173/login?verified=success");

        } catch (Exception e) {
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            response.sendRedirect("http://localhost:5173/login?error=invalid_token");
        }
    }

    // 3. Đăng nhập (Bước 1: Nhập mail pass -> Gửi OTP)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody UserLoginDTO userLoginDTO) throws Exception {

        authService.login(userLoginDTO);

        ApiResponse apiResponse = ApiResponse.success("Chuyển đến trang verify Otp", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // 4. Xác thực OTP (Bước 2: Nhập OTP -> Lấy Token Login)
    @PostMapping("/verify_otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtpLogin (@RequestParam String otp, HttpServletResponse response){

        Map<String, Object> body = authService.verifyOtpLogin(otp, response);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Đăng nhập thành công", body);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}