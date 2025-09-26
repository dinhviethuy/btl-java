package com.fullnestjob.modules.permissions.repo;

import com.fullnestjob.modules.permissions.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    Page<Permission> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Permission> findByApiPathContainingIgnoreCase(String apiPath, Pageable pageable);
    Page<Permission> findByMethodContainingIgnoreCase(String method, Pageable pageable);
    Page<Permission> findByModuleContainingIgnoreCase(String module, Pageable pageable);
    Page<Permission> findByNameContainingIgnoreCaseAndApiPathContainingIgnoreCase(String name, String apiPath, Pageable pageable);
    Optional<Permission> findByApiPathAndMethodAndModule(String apiPath, String method, String module);

    // Comprehensive filter methods
    @Query("select p from Permission p where (:name is null or lower(p.name) like lower(concat('%', :name, '%'))) and (:apiPath is null or lower(p.apiPath) like lower(concat('%', :apiPath, '%'))) and (:method is null or lower(p.method) like lower(concat('%', :method, '%'))) and (:module is null or lower(p.module) like lower(concat('%', :module, '%')))")
    Page<Permission> findByAllFilters(@Param("name") String name, @Param("apiPath") String apiPath, @Param("method") String method, @Param("module") String module, Pageable pageable);
}


