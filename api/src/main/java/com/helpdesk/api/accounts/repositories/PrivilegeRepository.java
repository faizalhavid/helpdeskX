package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.Privilege;
import com.helpdesk.api.accounts.entities.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    boolean existsByResourceIgnoreCaseAndAction(String resource, ActionType action);
    boolean existsByResourceIgnoreCaseAndActionAndIdNot(String resource, ActionType action, UUID id);
    Page<Privilege> findAllByIsDeletedFalse(Pageable pageable);
}
