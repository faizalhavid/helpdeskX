package com.helpdesk.api.accounts.dto.requests;

import com.helpdesk.api.accounts.entities.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @param resource case-insensitive, stored upper case, e.g. {@code incident} becomes {@code INCIDENT}
 * @param action   one of {@link ActionType}, e.g. {@code READ}
 */
public record PrivilegeRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$",
                message = "Resource may only contain letters, digits and underscores, e.g. USER_GROUP")
        String resource,
        @NotNull(message = "Action is required")
        ActionType action,
        @Size(max = 255) String description
) {
}
