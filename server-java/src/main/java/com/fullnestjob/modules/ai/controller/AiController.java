package com.fullnestjob.modules.ai.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.ai.dto.AiDtos.ChatRequestDTO;
import com.fullnestjob.modules.ai.dto.AiDtos.ChatResponseDTO;
import com.fullnestjob.modules.ai.dto.AiDtos.SuggestJobsRequestDTO;
import com.fullnestjob.modules.ai.dto.AiDtos.SuggestJobsResponseDTO;
import com.fullnestjob.modules.ai.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    @Message("Chat processed successfully")
    public ResponseEntity<ChatResponseDTO> chat(@Valid @RequestBody ChatRequestDTO body) {
        return ResponseEntity.ok(aiService.handleChat(body));
    }

    @PostMapping("/suggest-jobs")
    @Message("Suggested jobs fetched successfully")
    public ResponseEntity<SuggestJobsResponseDTO> suggestJobs(@Valid @RequestBody SuggestJobsRequestDTO body) {
        return ResponseEntity.ok(aiService.suggestJobs(body));
    }
}


