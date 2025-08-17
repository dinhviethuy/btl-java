package com.fullnestjob.modules.auth.repo;

import com.fullnestjob.modules.auth.entity.RegistrationOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp, String> {
    Optional<RegistrationOtp> findByEmail(String email);
    void deleteByEmail(String email);
}


