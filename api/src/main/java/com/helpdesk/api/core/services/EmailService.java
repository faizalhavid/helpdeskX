package com.helpdesk.api.core.services;

import com.helpdesk.api.accounts.configs.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;

    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.mailFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
