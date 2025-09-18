package com.fullnestjob.modules.permissions.repo;

import com.fullnestjob.modules.permissions.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    Page<Permission> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Permission> findByApiPathContainingIgnoreCase(String apiPath, Pageable pageable);
    Page<Permission> findByMethodContainingIgnoreCase(String method, Pageable pageable);
    Page<Permission> findByModuleContainingIgnoreCase(String module, Pageable pageable);
    Page<Permission> findByNameContainingIgnoreCaseAndApiPathContainingIgnoreCase(String name, String apiPath, Pageable pageable);
    Optional<Permission> findByApiPathAndMethodAndModule(String apiPath, String method, String module);
}


