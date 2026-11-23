package com.helpdesk.api.core.models;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditMetadataDTO(
        UUID id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isDeleted,
        LocalDateTime deletedAt
) {

    public static AuditMetadataDTO from(BaseEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuditMetadataDTO(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                Boolean.TRUE.equals(entity.getIsDeleted()),
                entity.getDeletedAt()
        );
    }
}
