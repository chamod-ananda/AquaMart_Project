package com.example.backend.controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailSenderServiceController {
    private final JavaMailSender mailSender;

    public EmailSenderServiceController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Send an request email to the pet owner

    public void sendRequestEmail(String ownerEmail, Long petId, String messageText, String adopterEmail){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

            helper.setFrom("infaquamart@gmail.com");
            helper.setTo(ownerEmail);
            helper.setSubject("💛 New Request for Item #" + petId);

            String body = "<div style='font-family:Arial,sans-serif; color:#333;'>"
                    + "<h2 style='color:#1E90FF;'>You have a new request!</h2>"
                    + "<p>Your item with ID <strong>" + petId + "</strong> has received a request.</p>"
                    + "<p><strong>Message from adopter (" + escapeHtml(adopterEmail) + "):</strong></p>"
                    + "<div style='padding:12px; border-left:4px solid #1E90FF; background:#f8f9ff;'>"
                    + "<p>" + (messageText==null||messageText.isBlank()? "<em>(no message)</em>": escapeHtml(messageText)) + "</p>"
                    + "</div>"
                    + "<p>You can contact the adopter directly at: <strong>" + escapeHtml(adopterEmail) + "</strong></p>"
                    + "<p>Thank you,<br><strong>MeowMate Team</strong></p>"
                    + "</div>";

            helper.setText(body,true);
            mailSender.send(message);
        } catch(MessagingException e){
            log.error("Failed to send request email to {} for pet {}", ownerEmail, petId, e);
        }
    }

    private String escapeHtml(String s){
        if(s==null) return "";
        return s.replaceAll("&","&amp;")
                .replaceAll("<","&lt;")
                .replaceAll(">","&gt;")
                .replaceAll("\"","&quot;")
                .replaceAll("'","&#39;");
    }

}
