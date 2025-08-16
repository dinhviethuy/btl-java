package com.fullnestjob.modules.resumes.repo;

import com.fullnestjob.modules.resumes.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, String> {

    @Query("select r from Resume r where r.companyId._id = :companyId")
    List<Resume> findByCompanyId(@Param("companyId") String companyId);

    List<Resume> findByUserId(String userId);

    Page<Resume> findByStatusContainingIgnoreCase(String status, Pageable pageable);

    @Query("select r from Resume r where upper(r.status) in :statuses")
    Page<Resume> findByStatusInIgnoreCase(@Param("statuses") List<String> statuses, Pageable pageable);
}


