package com.dhami.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "Recipient email must not be blank")
    @Email(message = "Invalid recipient email format")
    private String to;

    @NotBlank(message = "Subject must not be blank")
    private String subject;

    @NotBlank(message = "Body must not be blank")
    private String body;

    private List<@Email(message = "Invalid CC email format") String> cc;

    private List<@Email(message = "Invalid BCC email format") String> bcc;

    private boolean html;
}
