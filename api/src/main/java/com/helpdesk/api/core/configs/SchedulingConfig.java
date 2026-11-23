package com.helpdesk.api.core.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables {@code @Scheduled} jobs, e.g. the expired-session cleanup. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
