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

    @Query("select j from Job j where j.company._id = :companyId")
    Page<Job> findByCompanyId(@Param("companyId") String companyId, Pageable pageable);

    @Query("select j from Job j where j.company._id = :companyId and lower(j.name) like lower(concat('%', :name, '%'))")
    Page<Job> findByCompanyIdAndNameLike(@Param("companyId") String companyId, @Param("name") String name, Pageable pageable);

    @Query("select j from Job j where j.company._id = :companyId and lower(j.location) like lower(concat('%', :location, '%'))")
    Page<Job> findByCompanyIdAndLocationLike(@Param("companyId") String companyId, @Param("location") String location, Pageable pageable);

    @Query("select j from Job j where j.company._id = :companyId and lower(j.name) like lower(concat('%', :name, '%')) and lower(j.location) like lower(concat('%', :location, '%'))")
    Page<Job> findByCompanyIdAndNameLikeAndLocationLike(@Param("companyId") String companyId, @Param("name") String name, @Param("location") String location, Pageable pageable);

    @Query("select j from Job j where j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP) and (:name is null or lower(j.name) like lower(concat('%', :name, '%'))) and (:location is null or lower(j.location) like lower(concat('%', :location, '%')))")
    Page<Job> findPublicJobs(@Param("name") String name, @Param("location") String location, Pageable pageable);
}


