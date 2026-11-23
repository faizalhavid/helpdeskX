package com.helpdesk.api.accounts.events;

import com.helpdesk.api.accounts.configs.AuthProperties;
import com.helpdesk.api.core.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AuthEmailListener {
    private static final Logger log = LoggerFactory.getLogger(AuthEmailListener.class);

    private final EmailService emailService;
    private final AuthProperties authProperties;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuthEmail(AuthEmailEvent event) {
        try {
            switch (event.kind()) {
                case EMAIL_VERIFICATION -> emailService.send(event.to(), "Verify your HelpdeskX email",
                        """
                        Hi %s,

                        Please confirm your email address by opening the link below:

                        %s

                        This link expires in %s. If you did not create an account, you can ignore this email.
                        """.formatted(event.username(), link("/verify-email", event.rawToken()),
                                authProperties.verificationTokenTtl()));
                case PASSWORD_RESET -> emailService.send(event.to(), "Reset your HelpdeskX password",
                        """
                        Hi %s,

                        We received a request to reset your password. Open the link below to choose a new one:

                        %s

                        This link expires in %s. If you did not request a reset, you can ignore this email; \
                        your password will not change.
                        """.formatted(event.username(), link("/reset-password", event.rawToken()),
                                authProperties.passwordResetTokenTtl()));
                case PASSWORD_CHANGED -> emailService.send(event.to(), "Your HelpdeskX password was changed",
                        """
                        Hi %s,

                        The password for your account was just changed. If this was not you, \
                        reset your password immediately and contact your administrator.
                        """.formatted(event.username()));
            }
        } catch (MailException e) {
            // Never log the token itself.
            log.error("Failed to send {} email to {}: {}", event.kind(), event.to(), e.getMessage());
        }
    }

    private String link(String path, String rawToken) {
        return UriComponentsBuilder.fromUriString(authProperties.frontendBaseUrl())
                .path(path)
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();
    }
}
