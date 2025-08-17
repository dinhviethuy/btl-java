package com.fullnestjob.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
        public Integer age;
        public String address;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginResponseDTO {
        public String access_token;
        @JsonIgnore
        public String refresh_token;
        public UserNestedDto user;
    }

    public static class UpdateProfileDTO {
        public String name;
        public Integer age;
        public String address;
    }

    public static class ChangePasswordDTO {
        @NotBlank
        public String pass;
        @NotBlank
        @Size(min = 6)
        public String newPass;
        @NotBlank
        public String confirmNewPass;
    }

    public static class ForgotSendOtpDTO {
        @NotBlank
        @Email
        public String email;
    }

    public static class ForgotResetDTO {
        @NotBlank
        @Email
        public String email;
        @NotBlank
        public String otp;
        @NotBlank
        @Size(min = 6)
        public String newPassword;
    }

    public static class RegisterSendOtpDTO {
        @NotBlank
        public String name;
        @NotBlank
        @Email
        public String email;
        @NotBlank
        @Size(min = 6)
        public String password;
        @NotBlank
        public String confirmPassword;
        public Integer age;
        public String gender;
        public String address;
    }

    public static class RegisterVerifyDTO {
        @NotBlank
        @Email
        public String email;
        @NotBlank
        public String otp;
    }

    public static class RegisterVerifyAllDTO {
        @NotBlank
        public String name;
        @NotBlank
        @Email
        public String email;
        @NotBlank
        @Size(min = 6)
        public String password;
        @NotBlank
        public String confirmPassword;
        public Integer age;
        public String gender;
        public String address;
        @NotBlank
        public String otp;
    }
}


