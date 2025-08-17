package com.fullnestjob.modules.resumes.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.resumes.dto.ResumeDtos.CreateResumeBodyDTO;
import com.fullnestjob.modules.resumes.dto.ResumeDtos.ResumeDetailDTO;
import com.fullnestjob.modules.resumes.dto.ResumeDtos.UpdateResumeBodyDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fullnestjob.modules.resumes.service.ResumesService;
import com.fullnestjob.security.SecurityUtils;

@RestController
@RequestMapping("/v1/resumes")
public class ResumesController {

    private final ResumesService resumesService;

    public ResumesController(ResumesService resumesService) {
        this.resumesService = resumesService;
    }

    @GetMapping("/by-user")
    @Message("Resume fetched successfully")
    public ResponseEntity<java.util.List<ResumeDetailDTO>> findByUserId() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(resumesService.findAllByUserId(userId));
    }

    @GetMapping("/{id}")
    @Message("Resume fetched successfully")
    public ResponseEntity<ResumeDetailDTO> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(resumesService.findById(id));
    }

    @GetMapping
    @Message("Get resumes successfully")
    public ResponseEntity<PageResultDTO<ResumeDetailDTO>> find(@Valid @ModelAttribute PaginationQueryDTO query) {
        return ResponseEntity.ok(resumesService.find(query));
    }

    @PostMapping
    @Message("Resume created successfully")
    public ResponseEntity<ResumeDetailDTO> create(@Valid @RequestBody CreateResumeBodyDTO body) {
        String userId = SecurityUtils.getCurrentUserId();
        String email = com.fullnestjob.security.SecurityUtils.getCurrentEmail();
        return ResponseEntity.ok(resumesService.create(userId, email, body));
    }

    @PatchMapping("/{id}")
    @Message("Resume updated successfully")
    public ResponseEntity<ResumeDetailDTO> update(@PathVariable("id") String id, @RequestBody UpdateResumeBodyDTO body) {
        String userId = SecurityUtils.getCurrentUserId();
        String email = "";
        return ResponseEntity.ok(resumesService.update(id, email, userId, body));
    }

    @DeleteMapping("/{id}")
    @Message("Resume deleted successfully")
    public ResponseEntity<ResumeDetailDTO> delete(@PathVariable("id") String id) {
        ResumeDetailDTO dto = resumesService.findById(id);
        resumesService.delete(id);
        return ResponseEntity.ok(dto);
    }
}


