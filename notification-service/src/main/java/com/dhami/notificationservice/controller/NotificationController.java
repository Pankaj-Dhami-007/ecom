package com.dhami.notificationservice.controller;

import com.dhami.notificationservice.dto.EmailRequest;
import com.dhami.notificationservice.dto.EmailResponse;
import com.dhami.notificationservice.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("REST request to send email to: {}", request.getTo());
        var response = emailService.sendEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email/async")
    public ResponseEntity<CompletableFuture<EmailResponse>> sendEmailAsync(@Valid @RequestBody EmailRequest request) {
        log.info("REST request to send email asynchronously to: {}", request.getTo());
        var futureResponse = emailService.sendEmailAsync(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(futureResponse);
    }
}
