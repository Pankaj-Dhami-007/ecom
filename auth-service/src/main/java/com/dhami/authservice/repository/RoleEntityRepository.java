package com.dhami.authservice.repository;

import com.dhami.authservice.entity.RoleEntity;
import com.dhami.authservice.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName roleName);
}