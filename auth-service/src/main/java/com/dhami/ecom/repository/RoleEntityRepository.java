package com.dhami.ecom.repository;

import com.dhami.ecom.entity.RoleEntity;
import com.dhami.ecom.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName roleName);
}