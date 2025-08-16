package com.fullnestjob.modules.companies.repo;

import com.fullnestjob.modules.companies.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByName(String name);

    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Company> findByAddressContainingIgnoreCase(String address, Pageable pageable);
    Page<Company> findByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(String name, String address, Pageable pageable);
}


