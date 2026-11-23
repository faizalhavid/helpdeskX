package com.helpdesk.api.accounts.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * @param defaultRole           role assigned to self-registered users
 * @param verificationTokenTtl  lifetime of email verification links
 * @param passwordResetTokenTtl lifetime of password reset links
 * @param frontendBaseUrl       base URL used to build the links in emails
 * @param mailFrom              sender address of outgoing emails
 */
@ConfigurationProperties(prefix = "application.auth")
public record AuthProperties(
        @DefaultValue("requester") String defaultRole,
        @DefaultValue("24h") Duration verificationTokenTtl,
        @DefaultValue("30m") Duration passwordResetTokenTtl,
        @DefaultValue("http://localhost:3000") String frontendBaseUrl,
        @DefaultValue("no-reply@helpdeskx.local") String mailFrom
) {
}
