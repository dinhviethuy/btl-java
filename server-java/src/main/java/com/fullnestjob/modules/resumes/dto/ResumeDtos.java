package com.fullnestjob.modules.resumes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

public class ResumeDtos {
    public static class ActorDTO {
        public String _id;
        @Email
        public String email;
    }

    public static class ResumeHistoryDTO {
        public String status;
        public Date updatedAt;
        public ActorDTO updatedBy;
    }

    public static class CompanyIdDTO {
        public String _id;
        public String name;
        public String logo;
    }

    public static class JobIdDTO {
        public String _id;
        public String name;
    }

    public static class ResumeDetailDTO {
        public String _id;
        @Email
        public String email;
        public String userId;
        public String url;
        public String status;
        public Object companyId; // string or CompanyIdDTO
        public Object jobId; // string or JobIdDTO
        public List<ResumeHistoryDTO> history;
        public Date createdAt;
        public Date updatedAt;
        public Date deletedAt;
    }

    public static class CreateResumeBodyDTO {
        @NotBlank
        public String url;
        @NotBlank
        public String companyId;
        @NotBlank
        public String jobId;
    }

    public static class UpdateResumeBodyDTO {
        @NotBlank
        public String status;
    }
}


