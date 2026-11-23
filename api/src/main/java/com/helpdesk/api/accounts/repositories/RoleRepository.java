package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    Page<Role> findAllByIsDeletedFalse(Pageable pageable);
    List<Role> findAllByPrivilegesId(UUID privilegeId);
    Optional<Role> findByNameIgnoreCaseAndIsDeletedFalse(String name);
}
