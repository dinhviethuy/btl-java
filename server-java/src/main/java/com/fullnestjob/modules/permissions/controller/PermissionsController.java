package com.fullnestjob.modules.permissions.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.permissions.dto.PermissionDtos.CreatePermissionDTO;
import com.fullnestjob.modules.permissions.dto.PermissionDtos.PermissionDetailDTO;
import com.fullnestjob.modules.permissions.dto.PermissionDtos.UpdatePermissionDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fullnestjob.modules.permissions.service.PermissionsService;

@RestController
@RequestMapping("/v1/permissions")
public class PermissionsController {

    private final PermissionsService permissionsService;

    public PermissionsController(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @GetMapping
    @Message("Companies fetched successfully")
    public ResponseEntity<PageResultDTO<PermissionDetailDTO>> find(@Valid @ModelAttribute PaginationQueryDTO query) {
        return ResponseEntity.ok(permissionsService.find(query));
    }

    @GetMapping("/{id}")
    @Message("Company fetched successfully")
    public ResponseEntity<PermissionDetailDTO> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(permissionsService.findById(id));
    }

    @PostMapping
    @Message("Company created successfully")
    public ResponseEntity<PermissionDetailDTO> create(@Valid @RequestBody CreatePermissionDTO body) {
        return ResponseEntity.ok(permissionsService.create(body));
    }

    @PatchMapping("/{id}")
    @Message("Company updated successfully")
    public ResponseEntity<PermissionDetailDTO> update(@PathVariable("id") String id, @RequestBody UpdatePermissionDTO body) {
        return ResponseEntity.ok(permissionsService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @Message("Company deleted successfully")
    public ResponseEntity<PermissionDetailDTO> delete(@PathVariable("id") String id) {
        PermissionDetailDTO dto = permissionsService.findById(id);
        permissionsService.delete(id);
        return ResponseEntity.ok(dto);
    }
}


