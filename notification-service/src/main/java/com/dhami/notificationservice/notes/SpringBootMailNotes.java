package com.dhami.notificationservice.notes;

/**
 * ============================================================================
 *                      SPRING BOOT EMAIL (SMTP) NOTES
 * ============================================================================
 *
 * WHAT IS EMAIL SENDING?
 * ----------------------------------------------------------------------------
 * Sending an email is similar to sending a physical letter.
 *
 * Real Life Example:
 *
 * You (Sender)
 *      |
 *      | Write a letter
 *      v
 * Post Office
 *      |
 *      | Delivers the letter
 *      v
 * Receiver
 *
 *
 * In Software:
 *
 * Spring Boot App
 *      |
 *      | Creates Email
 *      v
 * SMTP Server
 *      |
 *      | Delivers Email
 *      v
 * Receiver Inbox
 *
 *
 * ============================================================================
 * WHAT IS SMTP?
 * ============================================================================
 *
 * SMTP = Simple Mail Transfer Protocol
 *
 * Protocol = A set of rules that computers follow to communicate.
 *
 * SMTP is the protocol used for SENDING emails.
 *
 * Think of SMTP as a digital post office.
 *
 * Example:
 *
 * Spring Boot App
 *        |
 *        |
 *        v
 * smtp.gmail.com
 *        |
 *        |
 *        v
 * recipient@gmail.com
 *
 * The SMTP server receives your email and delivers it.
 *
 *
 * ============================================================================
 * WHAT IS AN SMTP SERVER?
 * ============================================================================
 *
 * An SMTP Server is a machine/service responsible for accepting and
 * forwarding emails.
 *
 * Common SMTP Servers:
 *
 * Gmail   -> smtp.gmail.com
 * Outlook -> smtp.office365.com
 * Yahoo   -> smtp.mail.yahoo.com
 *
 * When we configure:
 *
 * host=smtp.gmail.com
 *
 * We are telling Spring Boot:
 *
 * "Use Gmail's mail server to send emails."
 *
 *
 * ============================================================================
 * WHAT IS HOST?
 * ============================================================================
 *
 * Host = Address of the server.
 *
 * Similar to:
 *
 * google.com
 * youtube.com
 * amazon.com
 *
 * For email:
 *
 * smtp.gmail.com
 *
 * This is the address where Spring Boot connects.
 *
 *
 * ============================================================================
 * WHAT IS A PORT?
 * ============================================================================
 *
 * A server can run multiple services.
 *
 * Each service listens on a different port.
 *
 * Real Life Example:
 *
 * Building Address:
 *      Main Building
 *
 * Flat Numbers:
 *      Flat 101
 *      Flat 102
 *      Flat 103
 *
 * Here:
 *      Building = Server
 *      Flat Number = Port
 *
 * Common Ports:
 *
 * HTTP  -> 80
 * HTTPS -> 443
 * SMTP  -> 587
 *
 * Example:
 *
 * smtp.gmail.com:587
 *
 * Meaning:
 *
 * Connect to Gmail SMTP service using port 587.
 *
 *
 * ============================================================================
 * WHAT IS USERNAME?
 * ============================================================================
 *
 * Username is the email account used to authenticate with the SMTP server.
 *
 * Example:
 *
 * username=mycompany@gmail.com
 *
 * Spring Boot says:
 *
 * "I am mycompany@gmail.com"
 *
 *
 * ============================================================================
 * WHAT IS PASSWORD?
 * ============================================================================
 *
 * Password is used to authenticate the SMTP account.
 *
 * For Gmail:
 *
 * Usually an App Password is used.
 *
 * Example:
 *
 * password=abcd efgh ijkl mnop
 *
 * Why?
 *
 * Gmail generally blocks normal account passwords for SMTP access.
 *
 *
 * ============================================================================
 * WHAT IS AUTHENTICATION?
 * ============================================================================
 *
 * Authentication means proving your identity.
 *
 * Real Life Example:
 *
 * ATM Machine:
 *
 * Insert Card
 * Enter PIN
 *
 * Correct PIN?
 *      Yes -> Access Granted
 *      No  -> Access Denied
 *
 * SMTP works similarly:
 *
 * Send Username
 * Send Password
 *
 * Correct?
 *      Yes -> Email can be sent
 *      No  -> Authentication Failed
 *
 *
 * ============================================================================
 * WHAT IS TLS?
 * ============================================================================
 *
 * TLS = Transport Layer Security
 *
 * TLS encrypts communication between your application and the SMTP server.
 *
 * Without TLS:
 *
 * App ------------------> SMTP Server
 *
 * Data travels in readable form.
 *
 * Risk:
 * Someone may intercept and read data.
 *
 *
 * With TLS:
 *
 * App =====Encrypted=====> SMTP Server
 *
 * Data becomes unreadable to attackers.
 *
 *
 * ============================================================================
 * WHAT IS STARTTLS?
 * ============================================================================
 *
 * STARTTLS upgrades a normal connection into an encrypted TLS connection.
 *
 * Flow:
 *
 * Spring Boot
 *      |
 *      | Connect
 *      v
 * SMTP Server
 *      |
 *      | STARTTLS Request
 *      v
 * Encrypted Connection Established
 *
 *
 * ============================================================================
 * WHAT DOES starttls.enable=true MEAN?
 * ============================================================================
 *
 * It tells JavaMail:
 *
 * "Use TLS encryption if the server supports it."
 *
 * Example:
 *
 * starttls.enable=true
 *
 * Result:
 *
 * Secure encrypted communication.
 *
 *
 * ============================================================================
 * WHAT DOES starttls.required=true MEAN?
 * ============================================================================
 *
 * It tells JavaMail:
 *
 * "TLS is mandatory."
 *
 * If the server cannot provide TLS:
 *
 * Connection will fail.
 *
 * Example:
 *
 * Server Supports TLS?
 *      Yes -> Continue
 *      No  -> Reject Connection
 *
 *
 * ============================================================================
 * WHAT DOES debug=true MEAN?
 * ============================================================================
 *
 * Enables detailed SMTP logs.
 *
 * Useful for troubleshooting.
 *
 * Example Logs:
 *
 * Connecting to smtp.gmail.com
 * Authentication successful
 * STARTTLS enabled
 * Sending message
 * Message sent
 *
 *
 * ============================================================================
 * WHAT DOES SPRING BOOT DO INTERNALLY?
 * ============================================================================
 *
 * When you call:
 *
 * javaMailSender.send(message);
 *
 * Internal Process:
 *
 * Step 1:
 * Connect to smtp.gmail.com:587
 *
 * Step 2:
 * Send Username and Password
 *
 * Step 3:
 * Perform Authentication
 *
 * Step 4:
 * Start TLS Encryption
 *
 * Step 5:
 * Upload Email Data
 *
 * Step 6:
 * SMTP Server Delivers Email
 *
 *
 * ============================================================================
 * COMPLETE EMAIL FLOW
 * ============================================================================
 *
 * Spring Boot App
 *         |
 *         |
 *         v
 * smtp.gmail.com
 * Port 587
 *         |
 *         |
 *         v
 * Authentication
 * (username + password)
 *         |
 *         |
 *         v
 * STARTTLS Encryption
 *         |
 *         |
 *         v
 * Email Sent
 *         |
 *         |
 *         v
 * Receiver Inbox
 *
 *
 * ============================================================================
 * SPRING BOOT CONFIG EXPLANATION
 * ============================================================================
 *
 * spring:
 *   mail:
 *     host: smtp.gmail.com
 *     port: 587
 *     username: your_email@gmail.com
 *     password: your_app_password
 *     properties:
 *       mail:
 *         smtp:
 *           auth: true
 *           starttls:
 *             enable: true
 *             required: true
 *         debug: false
 *
 *
 * host
 * ----
 * SMTP server address.
 *
 * port
 * ----
 * SMTP service port.
 *
 * username
 * --------
 * Email account used to log in.
 *
 * password
 * --------
 * Password/App Password for authentication.
 *
 * auth=true
 * ---------
 * Login is required before sending mail.
 *
 * starttls.enable=true
 * --------------------
 * Enable encrypted communication.
 *
 * starttls.required=true
 * ----------------------
 * Encryption is mandatory.
 *
 * debug=false
 * -----------
 * Disable SMTP debug logs.
 *
 *
 * ============================================================================
 * IMPORTANT INTERVIEW POINTS
 * ============================================================================
 *
 * Q. What is SMTP?
 * A. A protocol used to send emails.
 *
 * Q. What is smtp.gmail.com?
 * A. Gmail's SMTP server.
 *
 * Q. Why use port 587?
 * A. Standard secure SMTP submission port.
 *
 * Q. Why use TLS?
 * A. To encrypt communication.
 *
 * Q. Why is authentication required?
 * A. To verify the sender's identity.
 *
 * Q. What does JavaMailSender do?
 * A. Sends emails using SMTP from Spring Boot applications.
 *
 * ============================================================================
 */
public class SpringBootMailNotes {

    public static void main(String[] args) {

        // This class exists only for learning/documentation purposes.

    }
}