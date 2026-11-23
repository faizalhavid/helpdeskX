package com.helpdesk.api.accounts.dto.requests;

/** Same rule as {@link UserRequest#password()}; kept in one place for the auth DTOs. */
public final class PasswordPolicy {
    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String MESSAGE =
            "Password must be at least 8 characters with upper and lower case letters, a digit and one of @$!%*?&";

    private PasswordPolicy() {
    }
}
