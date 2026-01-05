package com.altspot.local.repository;

import com.altspot.local.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
        Role findByName(String roleName);

        boolean existsByName(String roleName);
}
