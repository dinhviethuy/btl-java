package com.fullnestjob.modules.roles.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.roles.dto.RoleDtos.CreateRoleDTO;
import com.fullnestjob.modules.roles.dto.RoleDtos.RoleDetailDTO;
import com.fullnestjob.modules.roles.dto.RoleDtos.UpdateRoleDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fullnestjob.modules.roles.service.RolesService;

@RestController
@RequestMapping("/v1/roles")
public class RolesController {

    private final RolesService rolesService;

    public RolesController(RolesService rolesService) {
        this.rolesService = rolesService;
    }

    @GetMapping
    @Message("Companies fetched successfully")
    public ResponseEntity<PageResultDTO<RoleDetailDTO>> find(@Valid @ModelAttribute PaginationQueryDTO query) {
        return ResponseEntity.ok(rolesService.find(query));
    }

    @GetMapping("/{id}")
    @Message("Company fetched successfully")
    public ResponseEntity<RoleDetailDTO> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(rolesService.findById(id));
    }

    @PostMapping
    @Message("Company created successfully")
    public ResponseEntity<RoleDetailDTO> create(@Valid @RequestBody CreateRoleDTO body) {
        return ResponseEntity.ok(rolesService.create(body));
    }

    @PatchMapping("/{id}")
    @Message("Company updated successfully")
    public ResponseEntity<RoleDetailDTO> update(@PathVariable("id") String id, @RequestBody UpdateRoleDTO body) {
        return ResponseEntity.ok(rolesService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @Message("Company deleted successfully")
    public ResponseEntity<RoleDetailDTO> delete(@PathVariable("id") String id) {
        RoleDetailDTO dto = rolesService.findById(id);
        rolesService.delete(id);
        return ResponseEntity.ok(dto);
    }
}


