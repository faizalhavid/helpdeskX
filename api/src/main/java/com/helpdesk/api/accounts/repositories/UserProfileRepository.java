package com.helpdesk.api.accounts.repositories;

import com.helpdesk.api.accounts.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    java.util.Optional<UserProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    Page<UserProfile> findAllByIsDeletedFalse(Pageable pageable);
}
