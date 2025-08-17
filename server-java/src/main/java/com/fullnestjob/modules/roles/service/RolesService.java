package com.fullnestjob.modules.roles.service;

import com.fullnestjob.modules.permissions.entity.Permission;
import com.fullnestjob.modules.permissions.repo.PermissionRepository;
import com.fullnestjob.modules.roles.dto.RoleDtos.CreateRoleDTO;
import com.fullnestjob.modules.roles.dto.RoleDtos.PermissionRefDTO;
import com.fullnestjob.modules.roles.dto.RoleDtos.RoleDetailDTO;
import com.fullnestjob.modules.roles.dto.RoleDtos.UpdateRoleDTO;
import com.fullnestjob.modules.roles.entity.Role;
import com.fullnestjob.modules.roles.repo.RoleRepository;
import com.fullnestjob.modules.shared.dto.PaginationDtos.MetaDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RolesService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolesService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public PageResultDTO<RoleDetailDTO> find(PaginationQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        Sort sort = parseSort(query);
        var pageable = PageRequest.of(current - 1, pageSize, sort);
        String name = query.getName();
        if (name != null) name = name.replaceAll("^/+|/i$", "");
        Page<Role> page = name != null ? roleRepository.findByNameContainingIgnoreCase(name, pageable)
                : roleRepository.findAll(pageable);
        PageResultDTO<RoleDetailDTO> res = new PageResultDTO<>();
        res.result = page.getContent().stream().map(this::toDetail).collect(Collectors.toList());
        MetaDTO meta = new MetaDTO();
        meta.current = current;
        meta.pageSize = pageSize;
        meta.total = (int) page.getTotalElements();
        meta.pages = page.getTotalPages();
        res.meta = meta;
        return res;
    }

    private Sort parseSort(PaginationQueryDTO query) {
        if (query == null) return Sort.by(Sort.Order.desc("updatedAt"));
        try {
            java.lang.reflect.Field f = query.getClass().getDeclaredField("sort");
            f.setAccessible(true);
            Object v = f.get(query);
            if (v instanceof String s && !s.isBlank()) {
                String key = s.startsWith("sort=") ? s.substring(5) : s;
                if (key.startsWith("-")) return Sort.by(Sort.Order.desc(key.substring(1)));
                return Sort.by(Sort.Order.asc(key));
            }
        } catch (Exception ignored) {}
        return Sort.by(Sort.Order.desc("updatedAt"));
    }

    public RoleDetailDTO findById(String id) {
        return roleRepository.findById(id).map(this::toDetail).orElse(null);
    }

    @Transactional
    public RoleDetailDTO create(CreateRoleDTO body) {
        Role r = new Role();
        r.setName(body.name);
        r.setDescription(body.description);
        r.setIsActive(body.isActive != null ? body.isActive : Boolean.TRUE);
        if (body.permissions != null) {
            List<Permission> perms = permissionRepository.findAllById(body.permissions);
            r.setPermissions(perms);
        }
        return toDetail(roleRepository.save(r));
    }

    @Transactional
    public RoleDetailDTO update(String id, UpdateRoleDTO body) {
        Role r = roleRepository.findById(id).orElseThrow();
        if (body.name != null) r.setName(body.name);
        if (body.description != null) r.setDescription(body.description);
        if (body.isActive != null) r.setIsActive(body.isActive);
        if (body.permissions != null) {
            List<Permission> perms = permissionRepository.findAllById(body.permissions);
            r.setPermissions(perms);
        }
        return toDetail(roleRepository.save(r));
    }

    @Transactional
    public void delete(String id) {
        Role r = roleRepository.findById(id).orElseThrow();
        if (r.getName() != null && r.getName().equalsIgnoreCase("SUPER_ADMIN")) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Cannot delete SUPER_ADMIN role");
        }
        roleRepository.deleteById(id);
    }

    private RoleDetailDTO toDetail(Role r) {
        RoleDetailDTO dto = new RoleDetailDTO();
        dto._id = r.get_id();
        dto.name = r.getName();
        dto.description = r.getDescription();
        dto.isActive = r.getIsActive();
        dto.createdAt = r.getCreatedAt();
        dto.updatedAt = r.getUpdatedAt();
        dto.deletedAt = r.getDeletedAt();
        if (r.getPermissions() != null) {
            dto.permissions = r.getPermissions().stream().map(p -> {
                PermissionRefDTO pr = new PermissionRefDTO();
                pr._id = p.get_id();
                pr.name = p.getName();
                pr.apiPath = p.getApiPath();
                pr.method = p.getMethod();
                pr.module = p.getModule();
                return pr;
            }).collect(Collectors.toList());
        }
        return dto;
    }
}


