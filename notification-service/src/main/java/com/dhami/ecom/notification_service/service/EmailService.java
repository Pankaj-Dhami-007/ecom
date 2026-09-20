package com.dhami.ecom.notification_service.service;

import com.dhami.ecom.notification_service.dto.EmailRequest;
import com.dhami.ecom.notification_service.dto.EmailResponse;
import com.dhami.ecom.notification_service.exception.EmailSendingException;
import com.dhami.ecom.notification_service.exception.InvalidEmailRequestException;
import com.dhami.ecom.notification_service.util.EmailValidator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("emailTaskExecutor")
    public CompletableFuture<EmailResponse> sendEmailAsync(EmailRequest request) {
        log.info("Async email send initiated for recipient: {}", request.getTo());
        try {
            var response = sendEmail(request);
            return CompletableFuture.completedFuture(response);
        } catch (Exception ex) {
            log.error("Async email send failed for recipient: {}", request.getTo(), ex);
            return CompletableFuture.completedFuture(
                    EmailResponse.builder()
                            .success(false)
                            .message("Email sending failed: " + ex.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
    }

    public EmailResponse sendEmail(EmailRequest request) {
        log.info("Processing email request for recipient: {}", request.getTo());

        validateRequest(request);

        try {
            var mimeMessage = buildMimeMessage(request);
            mailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}, subject: {}", request.getTo(), request.getSubject());
            return EmailResponse.builder()
                    .success(true)
                    .message("Email sent successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (MessagingException ex) {
            log.error("Failed to send email to: {}, subject: {}", request.getTo(), request.getSubject(), ex);
            throw new EmailSendingException("Failed to send email to " + request.getTo(), ex);
        }
    }

    private MimeMessage buildMimeMessage(EmailRequest request) throws MessagingException {
        log.debug("Building MIME message for recipient: {}", request.getTo());

        var mimeMessage = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());

        if (request.isHtml()) {
            helper.setText(request.getBody(), true);
        } else {
            helper.setText(request.getBody(), false);
        }

        if (request.getCc() != null && !request.getCc().isEmpty()) {
            var validCc = EmailValidator.filterInvalid(request.getCc());
            if (!validCc.isEmpty()) {
                helper.setCc(validCc.toArray(String[]::new));
                log.debug("Added CC recipients: {}", validCc);
            }
        }

        if (request.getBcc() != null && !request.getBcc().isEmpty()) {
            var validBcc = EmailValidator.filterInvalid(request.getBcc());
            if (!validBcc.isEmpty()) {
                helper.setBcc(validBcc.toArray(String[]::new));
                log.debug("Added BCC recipients: {}", validBcc);
            }
        }

        log.debug("MIME message built successfully for: {}", request.getTo());
        return mimeMessage;
    }

    private void validateRequest(EmailRequest request) {
        if (request == null) {
            throw new InvalidEmailRequestException("Email request must not be null");
        }

        if (!EmailValidator.isValid(request.getTo())) {
            log.warn("Invalid recipient email: {}", request.getTo());
            throw new InvalidEmailRequestException("Invalid recipient email: " + request.getTo());
        }

        log.debug("Email request validation passed for: {}", request.getTo());
    }
}
