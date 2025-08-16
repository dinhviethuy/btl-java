package com.fullnestjob.modules.permissions.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;

public class PermissionDtos {
    public static class PermissionDetailDTO {
        public String _id;
        public String name;
        public String apiPath;
        public String method;
        public String module;
        public Date createdAt;
        public Date updatedAt;
        public Date deletedAt;
    }

    public static class CreatePermissionDTO {
        @NotBlank
        public String name;
        @NotBlank
        public String apiPath;
        public String method;
        @NotBlank
        public String module;
    }

    public static class UpdatePermissionDTO {
        public String name;
        public String apiPath;
        public String method;
        public String module;
    }
}


