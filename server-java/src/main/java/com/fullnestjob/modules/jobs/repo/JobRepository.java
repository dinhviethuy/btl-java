package com.fullnestjob.modules.jobs.repo;

import com.fullnestjob.modules.jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {

	@Query("select j from Job j where j.company._id = :companyId")
	List<Job> findByCompanyId(@Param("companyId") String companyId);

    Page<Job> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Job> findByLocationContainingIgnoreCase(String location, Pageable pageable);
    Page<Job> findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(String name, String location, Pageable pageable);
}


