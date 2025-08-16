package com.fullnestjob.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class AuthDtos {
    public static class UserNestedRoleDto {
        public String _id;
        public String name;
    }

    public static class UserNestedPermissionDto {
        public String _id;
        public String name;
        public String apiPath;
        public String method;
        public String module;
    }

    public static class UserNestedDto {
        public String _id;
        @Email
        public String email;
        public String name;
        public Object role; // can be string or object
        public List<UserNestedPermissionDto> permissions;
    }

    public static class LoginBodyDTO {
        @NotBlank
        @Email
        public String username;
        @NotBlank
        @Size(min = 6)
        public String password;
    }

    public static class RegisterBodyDTO {
        @NotBlank
        public String name;
        @Email
        @NotBlank
        public String email;
        @NotBlank
        @Size(min = 6)
        public String password;
        public Integer age;
        public String gender;
        public String address;
    }

    public static class LoginResponseDTO {
        public String access_token;
        public UserNestedDto user;
    }
}


