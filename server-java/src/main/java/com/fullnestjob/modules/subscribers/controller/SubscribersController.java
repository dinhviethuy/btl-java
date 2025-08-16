package com.fullnestjob.modules.subscribers.controller;

import com.fullnestjob.modules.subscribers.dto.SubscribersDtos.CreateSubscribersBodyDTO;
import com.fullnestjob.modules.subscribers.dto.SubscribersDtos.SubscribersDetailDTO;
import com.fullnestjob.modules.subscribers.dto.SubscribersDtos.UpdateSubscribersBodyDTO;
import com.fullnestjob.modules.subscribers.service.SubscribersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fullnestjob.security.SecurityUtils;

@RestController
@RequestMapping("/v1/subscribers")
public class SubscribersController {

    private final SubscribersService subscribersService;

    public SubscribersController(SubscribersService subscribersService) {
        this.subscribersService = subscribersService;
    }

    @GetMapping
    public ResponseEntity<Object> find(@ModelAttribute com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO query) {
        return ResponseEntity.ok(subscribersService.find(query));
    }

    @PostMapping("/skills")
    public ResponseEntity<Object> findSkills(@RequestBody(required = false) java.util.Map<String, String> body) {
        String email = body != null && body.get("email") != null ? body.get("email") : SecurityUtils.getCurrentEmail();
        if (email == null || email.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Email is required");
        }
        return ResponseEntity.ok(subscribersService.findSkills(email));
    }

    @PostMapping
    public ResponseEntity<SubscribersDetailDTO> create(@Valid @RequestBody CreateSubscribersBodyDTO body) {
        String email = body.email != null ? body.email : SecurityUtils.getCurrentEmail();
        if (email == null || email.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Email is required");
        }
        return ResponseEntity.ok(subscribersService.create(body, email));
    }

    @PatchMapping
    public ResponseEntity<SubscribersDetailDTO> update(@Valid @RequestBody UpdateSubscribersBodyDTO body) {
        String email = body.email != null ? body.email : SecurityUtils.getCurrentEmail();
        if (email == null || email.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Email is required");
        }
        return ResponseEntity.ok(subscribersService.update(body, email));
    }

    @DeleteMapping
    public ResponseEntity<Object> delete() {
        String email = SecurityUtils.getCurrentEmail();
        if (email == null || email.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Email is required");
        }
        subscribersService.delete(email);
        return ResponseEntity.ok().build();
    }
}


