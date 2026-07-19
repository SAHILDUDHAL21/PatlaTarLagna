package com.patlatarlagna.repository;

import com.patlatarlagna.entity.Role;
import com.patlatarlagna.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
