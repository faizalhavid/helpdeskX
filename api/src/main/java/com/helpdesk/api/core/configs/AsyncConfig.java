package com.helpdesk.api.core.configs;

import com.helpdesk.api.accounts.configs.AuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Emails are sent asynchronously so SMTP latency never blocks (or leaks timing on) auth requests. */
@Configuration
@EnableAsync
@EnableConfigurationProperties(AuthProperties.class)
public class AsyncConfig {
}
