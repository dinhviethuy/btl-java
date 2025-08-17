package com.fullnestjob.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AiDtos {
    public static class ChatRequestDTO {
        @NotBlank
        public String message;
        public String email; // optional, to personalize
    }

    public static class ChatResponseDTO {
        public String reply;
    }

    public static class SuggestJobsRequestDTO {
        /**
         * free-text query from user, e.g. "React ở Hà Nội lương > 20tr"
         */
        @NotBlank
        public String query;
        public List<String> skills; // optional explicit filters
        public List<String> locations; // optional explicit filters
        public Integer current; // page
        public Integer pageSize;
    }

    public static class SuggestJobsResponseDTO {
        public com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO<com.fullnestjob.modules.jobs.dto.JobDtos.JobDetailDTO> jobs;
        public String reasoning; // brief explanation of how the query was interpreted
        public String normalizedQuery; // normalized filters -> query string
    }
}


