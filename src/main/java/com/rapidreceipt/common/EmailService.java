package com.rapidreceipt.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);

        if (purpose.equalsIgnoreCase("registration")) {
            message.setSubject("Welcome to RapidReceipt! Verify your email");
            message.setText("Your email verification OTP is: " + otp + "\n\nThis OTP will expire in 15 minutes.");
        } else if (purpose.equalsIgnoreCase("password_reset")) {
            message.setSubject("RapidReceipt - Password Reset OTP");
            message.setText("Your password reset OTP is: " + otp + "\n\nThis OTP will expire in 15 minutes. If you did not request this, please ignore this email.");
        }

        // For local testing before a real SMTP is configured
        System.out.println("==================================================");
        System.out.println("====== " + purpose.toUpperCase() + " OTP FOR " + toEmail + " ======");
        System.out.println("OTP: " + otp);
        System.out.println("==================================================");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ". Error: " + e.getMessage());
            // In a real production app, you might want to throw an exception or handle this more gracefully.
            // For now, we print it so it doesn't hard-crash the registration flow if the SMTP isn't configured yet.
        }
    }
}
