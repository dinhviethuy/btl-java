package com.fullnestjob.modules.jobs.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.jobs.dto.JobDtos.CreateJobBodyDTO;
import com.fullnestjob.modules.jobs.dto.JobDtos.JobDetailDTO;
import com.fullnestjob.modules.jobs.dto.JobDtos.UpdateJobBodyDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fullnestjob.modules.jobs.service.JobsService;

@RestController
@RequestMapping("/v1/jobs")
public class JobsController {

    private final JobsService jobsService;

    public JobsController(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    @GetMapping
    @Message("Get users successfully")
    public ResponseEntity<PageResultDTO<JobDetailDTO>> find(@Valid @ModelAttribute PaginationQueryDTO query) {
        return ResponseEntity.ok(jobsService.find(query));
    }

    @GetMapping("/{id}")
    @Message("Job fetched successfully")
    public ResponseEntity<JobDetailDTO> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(jobsService.findById(id));
    }

    @PostMapping
    @Message("Job created successfully")
    public ResponseEntity<JobDetailDTO> create(@Valid @RequestBody CreateJobBodyDTO body) {
        return ResponseEntity.ok(jobsService.create(body));
    }

    @PatchMapping("/{id}")
    @Message("Job created successfully")
    public ResponseEntity<JobDetailDTO> update(@PathVariable("id") String id, @RequestBody UpdateJobBodyDTO body) {
        return ResponseEntity.ok(jobsService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @Message("Job deleted successfully")
    public ResponseEntity<JobDetailDTO> delete(@PathVariable("id") String id) {
        JobDetailDTO dto = jobsService.findById(id);
        jobsService.delete(id);
        return ResponseEntity.ok(dto);
    }
}


