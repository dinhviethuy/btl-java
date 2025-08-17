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
}


