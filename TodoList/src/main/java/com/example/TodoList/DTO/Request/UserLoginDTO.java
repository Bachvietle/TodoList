package com.example.TodoList.DTO.Request;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String email;

    private String userPassword;
}
