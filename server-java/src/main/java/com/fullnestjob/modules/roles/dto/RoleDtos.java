package com.fullnestjob.modules.roles.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

public class RoleDtos {
    public static class PermissionRefDTO {
        public String _id;
        public String name;
        public String apiPath;
        public String method;
        public String module;
    }

    public static class RoleDetailDTO {
        public String _id;
        public String name;
        public String description;
        public Boolean isActive;
        public List<PermissionRefDTO> permissions; // for GET by id
        public Date createdAt;
        public Date updatedAt;
        public Date deletedAt;
    }

    public static class CreateRoleDTO {
        @NotBlank
        public String name;
        public String description;
        public Boolean isActive;
        public List<String> permissions; // create/update uses string ids
    }

    public static class UpdateRoleDTO {
        public String name;
        public String description;
        public Boolean isActive;
        public List<String> permissions;
    }
}


