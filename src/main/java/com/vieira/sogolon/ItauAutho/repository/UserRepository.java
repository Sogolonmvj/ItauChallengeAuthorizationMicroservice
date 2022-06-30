package com.vieira.sogolon.ItauAutho.repository;

import com.vieira.sogolon.ItauAutho.entity.UserCritic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserCritic, Long> {
    Optional<UserCritic> findByEmail(String email);
}
