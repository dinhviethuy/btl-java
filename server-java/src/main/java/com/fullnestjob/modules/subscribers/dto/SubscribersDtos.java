package com.fullnestjob.modules.subscribers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

public class SubscribersDtos {
    public static class SubscribersDetailDTO {
        public String _id;
        public String name;
        @Email
        public String email;
        public List<String> skills;
        public Date createdAt;
        public Date updatedAt;
        public Date deletedAt;
    }

    public static class CreateSubscribersBodyDTO {
        @NotBlank
        @Email
        public String email;
        public List<String> skills;
        public String name;
    }

    public static class UpdateSubscribersBodyDTO {
        public String name;
        public List<String> skills;
        @Email
        public String email;
    }
}


