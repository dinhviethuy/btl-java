package com.fullnestjob.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;

public class UserDtos {
    public static class CompanyRefDTO {
        public String _id;
        public String name;
    }

    public static class RoleRefDTO {
        public String _id;
        public String name;
    }

    public static class UserDetailDTO {
        public String _id;
        @Email
        public String email;
        public String name;
        public String avatar;
        public Integer age;
        public String gender;
        public RoleRefDTO role; // or string in auth
        public CompanyRefDTO company;
        public String address;
        public Date createdAt;
        public Date updatedAt;
        public Date deletedAt;
    }

    public static class CreateUserBodyDTO {
        @Email
        public String email;
        @NotBlank
        public String password;
        @NotBlank
        public String name;
        public Integer age;
        public String gender;
        public CompanyRefDTO company;
        public String role;
        public String address;
    }

    public static class UpdateUserBodyDTO {
        @Email
        public String email;
        public String name;
        public String avatar;
        public Integer age;
        public String gender;
        public CompanyRefDTO company;
        public String role;
        public String address;
    }
}


