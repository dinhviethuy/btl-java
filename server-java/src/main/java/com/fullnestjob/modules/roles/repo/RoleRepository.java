package com.fullnestjob.modules.roles.repo;

import com.fullnestjob.modules.roles.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameFetchPermissions(@Param("name") String name);
}


