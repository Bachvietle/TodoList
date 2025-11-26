package com.example.TodoList.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private String message;

    private T data;

    private boolean success;

    public static <T> ApiResponse<T> success(String message, T data){ // có <T> là để khai báo type parameter cho Generic method
        return new ApiResponse<>(message, data, true);
    }

    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>(message, null, false);
    }
}
