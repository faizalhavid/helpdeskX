package com.helpdesk.api.incidents.repositories;

import com.helpdesk.api.incidents.entities.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID>, JpaSpecificationExecutor<Incident> {
    Optional<Incident> getIncidentByIdAndIsDeleted(UUID id, Boolean isDeleted);
}
