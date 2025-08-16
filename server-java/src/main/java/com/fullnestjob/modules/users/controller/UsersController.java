package com.fullnestjob.modules.users.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import com.fullnestjob.modules.users.dto.UserDtos.CreateUserBodyDTO;
import com.fullnestjob.modules.users.dto.UserDtos.UpdateUserBodyDTO;
import com.fullnestjob.modules.users.dto.UserDtos.UserDetailDTO;
import com.fullnestjob.modules.users.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UsersController {
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping
    @Message("Get users successfully")
    public ResponseEntity<PageResultDTO<UserDetailDTO>> find(@Valid @ModelAttribute PaginationQueryDTO query) {
        return ResponseEntity.ok(usersService.find(query));
    }

    @GetMapping("/{id}")
    @Message("Get user successfully")
    public ResponseEntity<UserDetailDTO> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(usersService.findById(id));
    }

    @PostMapping
    @Message("Create user successfully")
    public ResponseEntity<UserDetailDTO> create(@Valid @RequestBody CreateUserBodyDTO body) {
        return ResponseEntity.ok(usersService.create(body));
    }

    @PatchMapping("/{id}")
    @Message("Update user successfully")
    public ResponseEntity<UserDetailDTO> update(@PathVariable("id") String id, @RequestBody UpdateUserBodyDTO body) {
        return ResponseEntity.ok(usersService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @Message("Delete user successfully")
    public ResponseEntity<UserDetailDTO> delete(@PathVariable("id") String id) {
        UserDetailDTO dto = usersService.findById(id);
        usersService.delete(id);
        return ResponseEntity.ok(dto);
    }
}


