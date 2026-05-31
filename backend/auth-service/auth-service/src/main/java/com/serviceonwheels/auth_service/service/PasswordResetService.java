package com.serviceonwheels.auth_service.service;

import com.serviceonwheels.auth_service.dto.ForgotPasswordRequest;
import com.serviceonwheels.auth_service.dto.ResetPasswordRequest;
import com.serviceonwheels.auth_service.model.PasswordResetToken;
import com.serviceonwheels.auth_service.model.User;
import com.serviceonwheels.auth_service.repository.PasswordResetTokenRepository;
import com.serviceonwheels.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;

/**
 * Handles forgot-password and reset-password flows.
 * <p>
 * Uses JavaMailSender to send real SMTP emails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final String FRONTEND_RESET_URL = "http://localhost:4200/reset-password?token=";

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    /**
     * Attempts to send a password reset email. If SMTP fails, throws a RuntimeException
     * which the controller can catch to notify the user.
     */
    public Map<String, String> handleForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Forgot-password requested for email: {}", email);

        // We only process if user exists, but we MUST throw if SMTP fails so the frontend knows.
        var userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            // Invalidate any previously issued tokens for this email
            tokenRepository.deleteAllByEmail(email);

            String rawToken = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .email(email)
                    .token(rawToken)
                    .createdAt(now)
                    .expiresAt(now.plusMinutes(TOKEN_EXPIRY_MINUTES))
                    .used(false)
                    .build();

            tokenRepository.save(resetToken);
            
            try {
                sendResetEmail(email, rawToken);
            } catch (MailException | MessagingException e) {
                log.error("Failed to send reset email via SMTP to {}: {}", email, e.getMessage());
                // Rethrow exception so the GlobalExceptionHandler catches it and returns a 500 error
                throw new RuntimeException("Unable to send email. Please verify SMTP configuration or try again later.", e);
            }
        }

        // Generic response — never reveal whether the account exists
        return Map.of("message", "If the account exists, reset instructions have been sent.");
    }

    /**
     * Validates the token and updates the user's password.
     */
    public Map<String, String> handleResetPassword(ResetPasswordRequest request) {
        String rawToken = request.getToken().trim();

        PasswordResetToken resetToken = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User account not found."));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Mark the token as used so it cannot be reused
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for email: {}", resetToken.getEmail());
        return Map.of("message", "Your password has been reset successfully. Please sign in with your new password.");
    }

    /**
     * Validates a token without consuming it — used by the frontend to check
     * whether the reset page should render or show an error.
     */
    public Map<String, String> validateToken(String rawToken) {
        PasswordResetToken resetToken = tokenRepository.findByToken(rawToken.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used.");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        return Map.of("message", "Token is valid.", "email", resetToken.getEmail());
    }

    /**
     * Sends the reset email via Spring Boot's JavaMailSender using an HTML template.
     */
    private void sendResetEmail(String email, String token) throws MailException, MessagingException {
        String resetUrl = FRONTEND_RESET_URL + token;
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(email);
        helper.setSubject("Reset Your Service on Wheels Password");
        
        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; color: #333;\">"
                + "<div style=\"text-align: center; margin-bottom: 30px;\">"
                + "<h2 style=\"color: #8B1E1E; margin: 0;\">Service on Wheels</h2>"
                + "</div>"
                + "<p style=\"font-size: 16px;\">Hello,</p>"
                + "<p style=\"font-size: 16px; line-height: 1.5;\">We received a request to reset the password for your Service on Wheels account. Click the button below to choose a new password:</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<a href=\"" + resetUrl + "\" style=\"background-color: #8B1E1E; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;\">Reset Password</a>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #666; margin-top: 30px;\">This password reset link is only valid for the next " + TOKEN_EXPIRY_MINUTES + " minutes.</p>"
                + "<p style=\"font-size: 14px; color: #666;\">If you did not request this password reset, please ignore this email. Your account remains secure.</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eaeaea; margin: 30px 0;\" />"
                + "<p style=\"font-size: 12px; color: #999; text-align: center;\">&copy; " + LocalDateTime.now().getYear() + " Service on Wheels. All rights reserved.</p>"
                + "</div>";
                
        helper.setText(htmlContent, true);

        log.info("Attempting to send HTML SMTP email to {}", email);
        mailSender.send(message);
        log.info("HTML SMTP email successfully dispatched to {}", email);
    }
}
