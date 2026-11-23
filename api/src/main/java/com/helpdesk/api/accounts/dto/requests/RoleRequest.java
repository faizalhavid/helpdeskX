package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

/**
 * @param privilegeIds {@code null} keeps the current privileges on update; an empty set clears them.
 */
public record RoleRequest(
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$",
                message = "Role may only contain letters, digits and underscores")
        String role,
        String description,
        Set<UUID> privilegeIds
) {
}
