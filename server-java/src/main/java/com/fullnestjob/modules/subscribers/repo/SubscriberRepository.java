package com.fullnestjob.modules.subscribers.repo;

import com.fullnestjob.modules.subscribers.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, String> {
    Optional<Subscriber> findByEmail(String email);
}


