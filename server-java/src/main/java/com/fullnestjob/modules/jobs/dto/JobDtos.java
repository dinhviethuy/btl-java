package com.fullnestjob.modules.jobs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class JobDtos {
    public static class CompanyNestedDTO {
        public String _id;
        public String name;
        public String logo;
    }

    public static class JobDetailDTO {
        public String _id;
        public String location;
        public String name;
        public List<String> skills;
        public Double salary;
        public Integer quantity;
        public String level;
        public String description;
        public Date startDate;
        public Date endDate;
        public Boolean isActive;
        public CompanyNestedDTO company;
        public Date createdAt;
        public Date updatedAt;
        public Date deletedAt;
    }

    public static class CreateJobBodyDTO {
        public String location;
        @NotBlank
        public String name;
        public List<String> skills;
        @NotNull
        public Double salary;
        @NotNull
        public Integer quantity;
        @NotBlank
        public String level;
        public String description;
        @NotNull
        public Date startDate;
        @NotNull
        public Date endDate;
        public Boolean isActive;
        public CompanyNestedDTO company;
    }

    public static class UpdateJobBodyDTO {
        public String location;
        public String name;
        public List<String> skills;
        public Double salary;
        public Integer quantity;
        public String level;
        public String description;
        public Date startDate;
        public Date endDate;
        public Boolean isActive;
        public CompanyNestedDTO company;
    }
}


