package com.vieira.sogolon.ItauAutho.repository;

import com.vieira.sogolon.ItauAutho.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}