package com.fullnestjob.modules.users.repo;

import com.fullnestjob.modules.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    @Query("select u from User u where u.company._id = :companyId")
    Page<User> findByCompanyId(@Param("companyId") String companyId, Pageable pageable);

    @Query("select u from User u where u.company._id = :companyId and lower(u.name) like lower(concat('%', :name, '%'))")
    Page<User> findByCompanyIdAndNameLike(@Param("companyId") String companyId, @Param("name") String name, Pageable pageable);

    @Query("select u from User u where u.company._id = :companyId and lower(u.email) like lower(concat('%', :email, '%'))")
    Page<User> findByCompanyIdAndEmailLike(@Param("companyId") String companyId, @Param("email") String email, Pageable pageable);

    @Query("select u from User u where u.company._id = :companyId and lower(u.name) like lower(concat('%', :name, '%')) and lower(u.email) like lower(concat('%', :email, '%'))")
    Page<User> findByCompanyIdAndNameLikeAndEmailLike(@Param("companyId") String companyId, @Param("name") String name, @Param("email") String email, Pageable pageable);

    // Stats: users created per day since
    @Query(value = "select date(created_at) as d, count(*) as c from users where created_at >= :from group by date(created_at) order by d", nativeQuery = true)
    java.util.List<Object[]> countCreatedPerDaySince(@Param("from") java.util.Date from);

    @Query(value = "select date(created_at) as d, count(*) as c from users where created_at >= :from and company_id in (:companyIds) group by date(created_at) order by d", nativeQuery = true)
    java.util.List<Object[]> countCreatedPerDaySinceByCompanyIds(@Param("from") java.util.Date from, @Param("companyIds") java.util.List<String> companyIds);

    @Query("select count(u) from User u where u.company._id in :companyIds")
    long countByCompanyIds(@Param("companyIds") java.util.List<String> companyIds);

    // Multi-company filters for users (company in list)
    @Query("select u from User u where u.company._id in :companyIds")
    Page<User> findByCompanyIds(@Param("companyIds") java.util.List<String> companyIds, Pageable pageable);

    @Query("select u from User u where u.company._id in :companyIds and lower(u.name) like lower(concat('%', :name, '%'))")
    Page<User> findByCompanyIdsAndNameLike(@Param("companyIds") java.util.List<String> companyIds, @Param("name") String name, Pageable pageable);

    @Query("select u from User u where u.company._id in :companyIds and lower(u.email) like lower(concat('%', :email, '%'))")
    Page<User> findByCompanyIdsAndEmailLike(@Param("companyIds") java.util.List<String> companyIds, @Param("email") String email, Pageable pageable);

    @Query("select u from User u where u.company._id in :companyIds and lower(u.name) like lower(concat('%', :name, '%')) and lower(u.email) like lower(concat('%', :email, '%'))")
    Page<User> findByCompanyIdsAndNameLikeAndEmailLike(@Param("companyIds") java.util.List<String> companyIds, @Param("name") String name, @Param("email") String email, Pageable pageable);
}


