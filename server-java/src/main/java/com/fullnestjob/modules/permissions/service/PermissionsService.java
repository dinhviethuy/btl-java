package com.fullnestjob.modules.permissions.service;

import com.fullnestjob.modules.permissions.dto.PermissionDtos.CreatePermissionDTO;
import com.fullnestjob.modules.permissions.dto.PermissionDtos.PermissionDetailDTO;
import com.fullnestjob.modules.permissions.dto.PermissionDtos.UpdatePermissionDTO;
import com.fullnestjob.modules.permissions.entity.Permission;
import com.fullnestjob.modules.permissions.repo.PermissionRepository;
import com.fullnestjob.modules.shared.dto.PaginationDtos.MetaDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class PermissionsService {
    private final PermissionRepository permissionRepository;

    public PermissionsService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public PageResultDTO<PermissionDetailDTO> find(PaginationQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        Sort sort = parseSort(query);
        var pageable = PageRequest.of(current - 1, pageSize, sort);
        String name = query.getName();
        String apiPath = query.getApiPath();
        String method = query.getMethod();
        String module = query.getModule();
        if (name != null) name = name.replaceAll("^/+|/i$", "");
        if (apiPath != null) apiPath = apiPath.replaceAll("^/+|/i$", "");
        if (method != null) method = method.replaceAll("^/+|/i$", "");
        if (method != null) method = method.toUpperCase();
        if (module != null) module = module.replaceAll("^/+|/i$", "");

        Page<Permission> page;
        // Sử dụng method kết hợp tất cả filter thay vì if-else
        page = permissionRepository.findByAllFilters(name, apiPath, method, module, pageable);
        PageResultDTO<PermissionDetailDTO> res = new PageResultDTO<>();
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

    public PermissionDetailDTO findById(String id) {
        return permissionRepository.findById(id).map(this::toDetail).orElse(null);
    }

    public PermissionDetailDTO create(CreatePermissionDTO body) {
        Permission p = new Permission();
        p.setName(body.name);
        p.setApiPath(body.apiPath);
        p.setMethod(body.method != null ? body.method.toUpperCase() : null);
        p.setModule(body.module != null ? body.module.toUpperCase() : null);
        return toDetail(permissionRepository.save(p));
    }

    public PermissionDetailDTO update(String id, UpdatePermissionDTO body) {
        Permission p = permissionRepository.findById(id).orElseThrow();
        if (body.name != null) p.setName(body.name);
        if (body.apiPath != null) p.setApiPath(body.apiPath);
        if (body.method != null) p.setMethod(body.method.toUpperCase());
        if (body.module != null) p.setModule(body.module.toUpperCase());
        return toDetail(permissionRepository.save(p));
    }

    public void delete(String id) {
        permissionRepository.deleteById(id);
    }

    private PermissionDetailDTO toDetail(Permission p) {
        PermissionDetailDTO dto = new PermissionDetailDTO();
        dto._id = p.get_id();
        dto.name = p.getName();
        dto.apiPath = p.getApiPath();
        dto.method = p.getMethod();
        dto.module = p.getModule();
        dto.createdAt = p.getCreatedAt();
        dto.updatedAt = p.getUpdatedAt();
        dto.deletedAt = p.getDeletedAt();
        return dto;
    }
}


