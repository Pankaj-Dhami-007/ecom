package com.dhami.notificationservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public final class EmailValidator {

    private static final Logger log = LoggerFactory.getLogger(EmailValidator.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private EmailValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static List<String> filterInvalid(List<String> emails) {
        if (emails == null) {
            return List.of();
        }
        return emails.stream()
                .filter(e -> {
                    boolean valid = isValid(e);
                    if (!valid) {
                        log.warn("Invalid email address filtered out: {}", e);
                    }
                    return valid;
                })
                .toList();
    }
}
