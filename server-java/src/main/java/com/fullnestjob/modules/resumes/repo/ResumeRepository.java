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

    @Query("select r from Resume r where r.jobId._id = :jobId")
    List<Resume> findByJobId(@Param("jobId") String jobId);

    List<Resume> findByUserId(String userId);

    Page<Resume> findByStatusContainingIgnoreCase(String status, Pageable pageable);

    @Query("select r from Resume r where upper(r.status) in :statuses")
    Page<Resume> findByStatusInIgnoreCase(@Param("statuses") List<String> statuses, Pageable pageable);

    @Query("select r from Resume r where r.companyId._id = :companyId")
    Page<Resume> findByCompanyId(@Param("companyId") String companyId, Pageable pageable);

    @Query("select r from Resume r where r.companyId._id = :companyId and lower(r.status) like lower(concat('%', :status, '%'))")
    Page<Resume> findByCompanyIdAndStatusLike(@Param("companyId") String companyId, @Param("status") String status, Pageable pageable);

    // Admin: search by optional companyName and jobName (case-insensitive contains)
    @Query("select r from Resume r left join r.companyId c left join r.jobId j where (:companyName is null or lower(c.name) like lower(concat('%', :companyName, '%'))) and (:jobName is null or lower(j.name) like lower(concat('%', :jobName, '%')))")
    Page<Resume> findByCompanyNameAndJobName(@Param("companyName") String companyName, @Param("jobName") String jobName, Pageable pageable);
}


