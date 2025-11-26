package com.example.TodoList.Service;

import com.example.TodoList.DTO.Request.UserLoginDTO;
import com.example.TodoList.Entity.RoleType;
import com.example.TodoList.Entity.User;
import com.example.TodoList.Entity.VerifyLoginOtp;
import com.example.TodoList.Exception.EmailExistsException;
import com.example.TodoList.Exception.InvalidOtpException;
import com.example.TodoList.Repository.OtpRepository;
import com.example.TodoList.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public void login(UserLoginDTO userLoginDTO) throws MessagingException {

        // Tạo auth request với email + password
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getUserPassword());

        try {

            // Authenticate qua manager (sẽ dùng provider để load user + check hashed password, tự động gọi các hàm override trong Entity User để ktra trạng thái tk (isEnabled))
            // Khi gọi .authenticate, AuthenticationManager chuyển giao lại việc authenticate cho các AuthenticationProvider (đã đc cấu hình Bean trong AppConfig)
            Authentication authentication = authenticationManager.authenticate(authRequest);

            // Nếu success (không throw exception), tiếp tục gửi OTP
            User user = (User) authentication.getPrincipal();  // Lấy user từ auth

            String otp = String.valueOf((int)( Math.random() * 900000) + 100000); // tạo số có 6 chữ số

            VerifyLoginOtp verifyLoginOtp = VerifyLoginOtp.builder()
                    .email(user.getEmail())
                    .otp(otp)
                    .expiryDate(LocalDateTime.now().plusMinutes(5))
                    .build();

            otpRepository.save(verifyLoginOtp);

            emailService.sendVerifyLoginMail(otp, user.getEmail());
        } catch (DisabledException e) {
            // Ném tiếp để GlobalHandler bắt (trả về 403)
            throw e;
        } catch (BadCredentialsException e){
            // Ném tiếp để GlobalHandler bắt (trả về 401)
            throw e;
        }
    }

    /*
    1. xác minh otp, đúng => cho login
    2. Tạo accessJwt, refreshJwt => đóng vào cookie gửi FE
     */

    public Map<String, Object> verifyOtpLogin(String otp, HttpServletResponse response){

        // Verify Otp
        VerifyLoginOtp verifyLoginOtp = otpRepository.findByOtp(otp).orElseThrow(
                () -> new InvalidOtpException("OTP không chính xác hoặc đã hết hạn")
        );

        if(verifyLoginOtp.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new InvalidOtpException("OTP không chính xác hoặc đã hết hạn");
        }

        User user = userRepository.findByEmail(verifyLoginOtp.getEmail()).orElseThrow(
                () -> new UsernameNotFoundException("Ko tìm thấy email")
        );

        otpRepository.delete(verifyLoginOtp);

        // Tạo access, refreshJwt
        String accessJwt = jwtService.generateAccessJwt(user);
        String refreshJwt = jwtService.generateRefreshJwt(user);

        // Refresh sẽ được đưa vào cookie và trả về Header response
        // Access được đưa lên controller và trả về Body response
        ResponseCookie cookie = ResponseCookie.from("refreshJwt", refreshJwt)
                .httpOnly(true) // Cookie sẽ không thể bị truy cập bởi JavaScript thông qua document.cookie secure
                .secure(false) // trong MT dev dùng http
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // Hạn 7 ngày
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, Object> body = new HashMap<>();

        body.put("accessJwt", accessJwt);
        body.put("user", Map.of(
                "email", user.getEmail(),
                "userName", user.getUserName()
        ));

        return body;
    }

    public void handleSuccessLoginGG(OAuth2User oAuth2User, HttpServletResponse response) throws IOException {


        String email = oAuth2User.getAttribute("email");

        String userName = oAuth2User.getAttribute("name");

        /*
        - Nếu tồn tại user (theo email) -> chỉ cập nhật các ttin, rồi cho login luôn
        - Nếu chưa thì tạo mới rồi login
         */
        User user = userRepository.findByEmail(email).map(existingUser -> { // map() trong Optional<> khác map() trong Stream
            // Update info:
                    existingUser.setUserName(userName);

                    return userRepository.save(existingUser);
                })
                .orElseGet(() ->{ // Chỉ chạy khi đtg Optional empty
                    User newUser = User.builder()
                            .email(email)
                            .userName(userName)
                            .userPassword("")
                            .role(RoleType.user)
                            .enabled(1)
                            .emailVerified(1)
                            .build();
                    return userRepository.save(newUser);
                });

        // Lại tạo Jwt như bth
        String accessJwt = jwtService.generateAccessJwt(user);
        String refreshJwt = jwtService.generateRefreshJwt(user);

        // Và set refreshJwt vào Cookie -> gửi vào Header response
        ResponseCookie cookie = ResponseCookie.from("refreshJwt", refreshJwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Còn accessJw + userInfo -> gửi vào Body response
        Map<String, Object> body = new HashMap<>();
        body.put("accessJwt", accessJwt);
        body.put("user", Map.of(
                "email", email,
                "userName", userName
        ));

        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(body.get("user"));

        // encoder userJson để tránh kí tự đặc biệt
        String encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8);

        String redirectUrl = "http://localhost:5173/login?google_token=" + accessJwt + "&google_user=" + encodedUserJson;

        response.sendRedirect(redirectUrl);
    }

}
