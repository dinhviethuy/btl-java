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

	// Multi-company filters for non-admin scope
	@Query("select j from Job j where j.company._id in :companyIds")
	Page<Job> findByCompanyIds(@Param("companyIds") java.util.List<String> companyIds, Pageable pageable);

	@Query("select j from Job j where j.company._id in :companyIds and lower(j.name) like lower(concat('%', :name, '%'))")
	Page<Job> findByCompanyIdsAndNameLike(@Param("companyIds") java.util.List<String> companyIds, @Param("name") String name, Pageable pageable);

	@Query("select j from Job j where j.company._id in :companyIds and lower(j.location) like lower(concat('%', :location, '%'))")
	Page<Job> findByCompanyIdsAndLocationLike(@Param("companyIds") java.util.List<String> companyIds, @Param("location") String location, Pageable pageable);

	@Query("select j from Job j where j.company._id in :companyIds and lower(j.name) like lower(concat('%', :name, '%')) and lower(j.location) like lower(concat('%', :location, '%'))")
	Page<Job> findByCompanyIdsAndNameLikeAndLocationLike(@Param("companyIds") java.util.List<String> companyIds, @Param("name") String name, @Param("location") String location, Pageable pageable);

    @Query("select j from Job j where j.company._id = :companyId and lower(j.name) like lower(concat('%', :name, '%'))")
    Page<Job> findByCompanyIdAndNameLike(@Param("companyId") String companyId, @Param("name") String name, Pageable pageable);

    @Query("select j from Job j where j.company._id = :companyId and lower(j.location) like lower(concat('%', :location, '%'))")
    Page<Job> findByCompanyIdAndLocationLike(@Param("companyId") String companyId, @Param("location") String location, Pageable pageable);

    @Query("select j from Job j where j.company._id = :companyId and lower(j.name) like lower(concat('%', :name, '%')) and lower(j.location) like lower(concat('%', :location, '%'))")
    Page<Job> findByCompanyIdAndNameLikeAndLocationLike(@Param("companyId") String companyId, @Param("name") String name, @Param("location") String location, Pageable pageable);

    @Query("select j from Job j where j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP) and (:name is null or lower(j.name) like lower(concat('%', :name, '%')) or lower(j.company.name) like lower(concat('%', :name, '%'))) and ((:location is null) or lower(j.location) like lower(concat('%', :location, '%'))) and (:excludeId is null or j._id <> :excludeId)")
    Page<Job> findPublicJobs(@Param("name") String name, @Param("location") String location, @Param("excludeId") String excludeId, Pageable pageable);

    @Query("select j from Job j where j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP) and j.company._id = :companyId")
    Page<Job> findPublicJobsByCompanyId(@Param("companyId") String companyId, Pageable pageable);

    @Query("select distinct j from Job j join j.skills s where j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP) and (:name is null or lower(j.name) like lower(concat('%', :name, '%'))) and ((:location is null) or lower(j.location) like lower(concat('%', :location, '%'))) and lower(s) in :skills and (:excludeId is null or j._id <> :excludeId)")
    Page<Job> findPublicJobsBySkills(@Param("name") String name, @Param("location") String location, @Param("skills") List<String> skills, @Param("excludeId") String excludeId, Pageable pageable);

    @Query("select distinct j from Job j where j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP) and (:name is null or lower(j.name) like lower(concat('%', :name, '%')) or lower(j.company.name) like lower(concat('%', :name, '%'))) and (:locations is null or lower(j.location) in :locations) and (:excludeId is null or j._id <> :excludeId)")
    Page<Job> findPublicJobsByLocations(@Param("name") String name, @Param("locations") List<String> locations, @Param("excludeId") String excludeId, Pageable pageable);

    @Query("select distinct j from Job j join j.skills s where j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP) and (:name is null or lower(j.name) like lower(concat('%', :name, '%'))) and (:locations is null or lower(j.location) in :locations) and lower(s) in :skills and (:excludeId is null or j._id <> :excludeId)")
    Page<Job> findPublicJobsBySkillsAndLocations(@Param("name") String name, @Param("skills") List<String> skills, @Param("locations") List<String> locations, @Param("excludeId") String excludeId, Pageable pageable);

    // Admin searching: optional levels filter, optional min salary filter
    @Query("select j from Job j where (:name is null or lower(j.name) like lower(concat('%', :name, '%'))) and ((:location is null) or lower(j.location) like lower(concat('%', :location, '%'))) and (:minSalary is null or j.salary >= :minSalary) and (:maxSalary is null or j.salary <= :maxSalary) and (:levels is null or exists (select l from j.levels l where upper(l) in :levels))")
    Page<Job> findAdminJobs(@Param("name") String name, @Param("location") String location, @Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary, @Param("levels") List<String> levels, Pageable pageable);

    // Count active and non-expired jobs for a company (for public/company cards)
    @Query("select count(j) from Job j where j.company._id = :companyId and j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP)")
    long countActivePublicByCompanyId(@Param("companyId") String companyId);

	// Multi-company counts (active public)
    @Query("select count(j) from Job j where j.company._id in :companyIds and j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP)")
	long countActivePublicByCompanyIds(@Param("companyIds") java.util.List<String> companyIds);

    // Count all open jobs
    @Query("select count(j) from Job j where j.isActive = true and (j.startDate is null or j.startDate <= CURRENT_TIMESTAMP) and (j.endDate is null or j.endDate >= CURRENT_TIMESTAMP)")
    long countActivePublicAll();

    // Stats: count jobs created per day since a date
    @Query(value = "select date(created_at) as d, count(*) as c from jobs where created_at >= :from group by date(created_at) order by d", nativeQuery = true)
    List<Object[]> countCreatedPerDaySince(@Param("from") java.util.Date from);

    @Query(value = "select date(created_at) as d, count(*) as c from jobs where created_at >= :from and company_id = :companyId group by date(created_at) order by d", nativeQuery = true)
    List<Object[]> countCreatedPerDaySinceByCompany(@Param("from") java.util.Date from, @Param("companyId") String companyId);

	@Query(value = "select date(created_at) as d, count(*) as c from jobs where created_at >= :from and company_id in (:companyIds) group by date(created_at) order by d", nativeQuery = true)
	java.util.List<Object[]> countCreatedPerDaySinceByCompanyIds(@Param("from") java.util.Date from, @Param("companyIds") java.util.List<String> companyIds);

    // Stats: count by level (ElementCollection levels)
    @Query("select upper(l) as lv, count(j) from Job j join j.levels l group by upper(l)")
    List<Object[]> countByLevels();

    @Query("select upper(l) as lv, count(j) from Job j join j.levels l where j.company._id = :companyId group by upper(l)")
    List<Object[]> countByLevelsForCompany(@Param("companyId") String companyId);

	@Query("select upper(l) as lv, count(j) from Job j join j.levels l where j.company._id in :companyIds group by upper(l)")
	java.util.List<Object[]> countByLevelsForCompanies(@Param("companyIds") java.util.List<String> companyIds);

    // Stats: top companies by open jobs
    @Query(value = "select c.id, c.name, c.logo, count(*) as cnt from jobs j join companies c on j.company_id = c.id where j.is_active = true and (j.start_date is null or j.start_date <= current_date()) and (j.end_date is null or j.end_date >= current_date()) group by c.id, c.name, c.logo order by cnt desc limit :limit", nativeQuery = true)
    List<Object[]> topCompaniesByOpenJobs(@Param("limit") int limit);

    @Query("select count(j) from Job j where j.company._id = :companyId")
    long countByCompanyId(@Param("companyId") String companyId);

	@Query("select count(j) from Job j where j.company._id in :companyIds")
	long countByCompanyIdIn(@Param("companyIds") java.util.List<String> companyIds);
}


