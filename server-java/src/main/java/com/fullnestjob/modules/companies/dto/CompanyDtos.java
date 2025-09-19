package com.fullnestjob.modules.companies.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;

public class CompanyDtos {
    public static class CompanyDetailDTO {
        public String _id;
        public String name;
        public String description;
        public String logo;
        public String address;
        // number of currently open jobs (active + within date range)
        public Long openJobs;
        public Date createdAt;
        public Date updatedAt;
        public Date deletedAt;
    }

    public static class CreateCompanyBodyDTO {
        @NotBlank
        public String name;
        @NotBlank
        public String description;
        @NotBlank
        public String address;
        public String logo;
    }

    public static class UpdateCompanyBodyDTO {
        public String name;
        public String description;
        public String address;
        public String logo;
    }
}


