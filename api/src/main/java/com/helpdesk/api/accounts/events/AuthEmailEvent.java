package com.helpdesk.api.accounts.events;

/**
 * Published inside an auth transaction; the email is sent only after that transaction commits,
 * so a rolled-back request never emails a link that does not exist.
 *
 * @param rawToken null for {@link Kind#PASSWORD_CHANGED}
 */
public record AuthEmailEvent(Kind kind, String to, String username, String rawToken) {
    public enum Kind {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        PASSWORD_CHANGED
    }
}
