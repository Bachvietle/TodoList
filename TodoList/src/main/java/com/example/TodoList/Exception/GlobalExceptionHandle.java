package com.example.TodoList.Exception;

import com.example.TodoList.DTO.Response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(TodoException.class)
    public ResponseEntity<ApiResponse> handleTodoException(TodoException ex){
        ApiResponse response = ApiResponse.error(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailExistsException.class)
    public ResponseEntity<ApiResponse> handleEmailExistsException(EmailExistsException ex){

        ApiResponse response = ApiResponse.error(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse> handleInvalidOtpException(InvalidOtpException ex){
        ApiResponse response = ApiResponse.error(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex){
        return new ResponseEntity<>(ApiResponse.error("Email hoặc mật khẩu không chính xác"), HttpStatus.UNAUTHORIZED);
    }

    // Xử lý tài khoản chưa kích hoạt (Disabled) -> 403 hoặc 401
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse> handleDisabledException(DisabledException ex){
        return new ResponseEntity<>(ApiResponse.error("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email!"), HttpStatus.FORBIDDEN);
    }

    // Xử lý lỗi Validation (@Valid trong DTO) -> 400
    // Cái này cực quan trọng để FE biết user nhập thiếu trường nào
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Trả về chuỗi lỗi đầu tiên hoặc list lỗi tùy bạn
        String errorMessage = "Dữ liệu không hợp lệ";

        if(!errors.isEmpty()){
            errorMessage = errors.values().iterator().next(); // Lấy lỗi đầu tiên
        }

        return new ResponseEntity<>(ApiResponse.error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    // Xử lý lỗi hệ thống
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex){
        ex.printStackTrace(); // Log lỗi ra console sever để fix bug

        return new ResponseEntity<>(ApiResponse.error("Lỗi hệ thống" + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
