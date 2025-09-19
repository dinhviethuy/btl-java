package com.fullnestjob.modules.companies.repo;

import com.fullnestjob.modules.companies.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByName(String name);

    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Company> findByAddressContainingIgnoreCase(String address, Pageable pageable);
    Page<Company> findByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(String name, String address, Pageable pageable);

    @Query("select c from Company c where (c.deletedAt is null) and (:name is null or lower(c.name) like lower(concat('%', :name, '%'))) and (:address is null or lower(c.address) like lower(concat('%', :address, '%')))")
    Page<Company> findPublicCompanies(@Param("name") String name, @Param("address") String address, Pageable pageable);

    // Stats: companies created per month since a date
    @Query(value = "select date_format(created_at, '%Y-%m') as m, count(*) as c from companies where created_at >= :from group by date_format(created_at, '%Y-%m') order by m", nativeQuery = true)
    java.util.List<Object[]> countCreatedPerMonthSince(@Param("from") java.util.Date from);

    // Companies created by a specific user (createdBy._id)
    @Query("select c from Company c where c.createdBy._id = :userId")
    java.util.List<Company> findAllByCreatorId(@Param("userId") String userId);
}


