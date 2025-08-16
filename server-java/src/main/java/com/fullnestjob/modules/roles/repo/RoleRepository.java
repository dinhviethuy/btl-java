package com.fullnestjob.modules.roles.repo;

import com.fullnestjob.modules.roles.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
}


