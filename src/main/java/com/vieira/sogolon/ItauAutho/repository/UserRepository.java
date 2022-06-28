package com.vieira.sogolon.ItauAutho.repository;

import com.vieira.sogolon.ItauAutho.domain.UserCritic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserCritic, Long> {
    UserCritic findByUsername(String username);
}
