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

    @Query("select r from Resume r where r.companyId._id in :companyIds")
    Page<Resume> findByCompanyIds(@Param("companyIds") java.util.List<String> companyIds, Pageable pageable);

    @Query("select r from Resume r where r.companyId._id = :companyId and lower(r.status) like lower(concat('%', :status, '%'))")
    Page<Resume> findByCompanyIdAndStatusLike(@Param("companyId") String companyId, @Param("status") String status, Pageable pageable);

    @Query("select r from Resume r where r.companyId._id in :companyIds and lower(r.status) like lower(concat('%', :status, '%'))")
    Page<Resume> findByCompanyIdsAndStatusLike(@Param("companyIds") java.util.List<String> companyIds, @Param("status") String status, Pageable pageable);

    @Query("select r from Resume r where r.companyId._id in :companyIds and upper(r.status) in :statuses")
    Page<Resume> findByCompanyIdsAndStatusInIgnoreCase(@Param("companyIds") java.util.List<String> companyIds, @Param("statuses") java.util.List<String> statuses, Pageable pageable);

    // Non-admin: filter by companyIds with optional companyName/jobName contains
    @Query("select r from Resume r left join r.companyId c left join r.jobId j where r.companyId._id in :companyIds and (:companyName is null or lower(c.name) like lower(concat('%', :companyName, '%'))) and (:jobName is null or lower(j.name) like lower(concat('%', :jobName, '%')))")
    Page<Resume> findByCompanyIdsAndCompanyNameAndJobName(@Param("companyIds") java.util.List<String> companyIds, @Param("companyName") String companyName, @Param("jobName") String jobName, Pageable pageable);

    // Non-admin: filter by specific companyId with jobName and status
    @Query("select r from Resume r left join r.jobId j where r.companyId._id = :companyId and (:jobName is null or lower(j.name) like lower(concat('%', :jobName, '%'))) and (:statuses is null or upper(r.status) in :statuses)")
    Page<Resume> findByCompanyIdAndJobNameAndStatusIn(@Param("companyId") String companyId, @Param("jobName") String jobName, @Param("statuses") java.util.List<String> statuses, Pageable pageable);

    // Non-admin: filter by companyIds with jobName and status
    @Query("select r from Resume r left join r.jobId j where r.companyId._id in :companyIds and (:jobName is null or lower(j.name) like lower(concat('%', :jobName, '%'))) and (:statuses is null or upper(r.status) in :statuses)")
    Page<Resume> findByCompanyIdsAndJobNameAndStatusIn(@Param("companyIds") java.util.List<String> companyIds, @Param("jobName") String jobName, @Param("statuses") java.util.List<String> statuses, Pageable pageable);

    // Admin: search by optional companyName and jobName (case-insensitive contains)
    @Query("select r from Resume r left join r.companyId c left join r.jobId j where (:companyName is null or lower(c.name) like lower(concat('%', :companyName, '%'))) and (:jobName is null or lower(j.name) like lower(concat('%', :jobName, '%')))")
    Page<Resume> findByCompanyNameAndJobName(@Param("companyName") String companyName, @Param("jobName") String jobName, Pageable pageable);

    // Admin: filter by companyId with jobName and status
    @Query("select r from Resume r left join r.jobId j where r.companyId._id = :companyId and (:jobName is null or lower(j.name) like lower(concat('%', :jobName, '%'))) and (:statuses is null or upper(r.status) in :statuses)")
    Page<Resume> findByCompanyIdAndJobNameAndStatusInAdmin(@Param("companyId") String companyId, @Param("jobName") String jobName, @Param("statuses") java.util.List<String> statuses, Pageable pageable);

    // Admin: filter by companyName, jobName and status
    @Query("select r from Resume r left join r.companyId c left join r.jobId j where (:companyName is null or lower(c.name) like lower(concat('%', :companyName, '%'))) and (:jobName is null or lower(j.name) like lower(concat('%', :jobName, '%'))) and (:statuses is null or upper(r.status) in :statuses)")
    Page<Resume> findByCompanyNameAndJobNameAndStatusInAdmin(@Param("companyName") String companyName, @Param("jobName") String jobName, @Param("statuses") java.util.List<String> statuses, Pageable pageable);

    // Stats: resumes per status
    @Query("select upper(coalesce(r.status, 'PENDING')), count(r) from Resume r group by upper(coalesce(r.status, 'PENDING'))")
    java.util.List<Object[]> countByStatus();

    // Stats: resumes per status for specific companies
    @Query("select upper(coalesce(r.status, 'PENDING')), count(r) from Resume r where r.companyId._id in :companyIds group by upper(coalesce(r.status, 'PENDING'))")
    java.util.List<Object[]> countByStatusForCompanies(@Param("companyIds") java.util.List<String> companyIds);

    // Stats: resumes per day since
    @Query(value = "select date(created_at) as d, count(*) as c from resumes where created_at >= :from group by date(created_at) order by d", nativeQuery = true)
    java.util.List<Object[]> countCreatedPerDaySince(@Param("from") java.util.Date from);

    @Query(value = "select date(created_at) as d, count(*) as c from resumes where created_at >= :from and company_id = :companyId group by date(created_at) order by d", nativeQuery = true)
    java.util.List<Object[]> countCreatedPerDaySinceByCompany(@Param("from") java.util.Date from, @Param("companyId") String companyId);

    @Query(value = "select date(created_at) as d, count(*) as c from resumes where created_at >= :from and company_id in (:companyIds) group by date(created_at) order by d", nativeQuery = true)
    java.util.List<Object[]> countCreatedPerDaySinceByCompanyIds(@Param("from") java.util.Date from, @Param("companyIds") java.util.List<String> companyIds);

    @Query("select count(r) from Resume r where r.companyId._id in :companyIds")
    long countByCompanyIds(@Param("companyIds") java.util.List<String> companyIds);
}


