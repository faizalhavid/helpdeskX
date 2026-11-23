package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    Page<UserGroup> findAllByIsDeletedFalse(Pageable pageable);
}
