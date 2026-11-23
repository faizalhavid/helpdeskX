package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    List<User> findAllByRoleId(UUID roleId);
    List<User> findAllByUserGroupId(UUID userGroupId);
    Page<User> findAllByIsDeletedFalse(Pageable pageable);

    /** Loads role and privileges in one query so authorities can be built outside a session. */
    @EntityGraph(attributePaths = {"role", "role.privileges"})
    Optional<User> findByUsernameIgnoreCaseAndIsDeletedFalse(String username);

    Optional<User> findByEmailIgnoreCaseAndIsDeletedFalse(String email);
}
