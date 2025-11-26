package com.example.TodoList.DTO.Request;

import lombok.Data;

@Data
public class UserRegisterDTO {

    private String email;

    private String userName;

    private String userPassword;
}
