package com.commercecore.auth.repository;

import com.commercecore.auth.entity.Role;
import com.commercecore.auth.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}