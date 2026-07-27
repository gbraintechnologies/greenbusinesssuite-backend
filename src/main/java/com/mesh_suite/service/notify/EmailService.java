package com.mesh_suite.service.notify;

import com.mesh_suite.domain.user.Users;
import com.mesh_suite.interceptor.TenantContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.UnsupportedEncodingException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.from-name:Mesh Team}")
    private String mailFromName;

    private String resolveVerificationLink(HttpServletRequest request, String email) {

        // Base URL from request: https://staging.meshsuites.com
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        // Get tenant dynamically
        String tenantId = TenantContext.getCurrentTenant();
        if (!StringUtils.hasText(tenantId)) {
            tenantId = "mesh-suite_db";   // or your preferred default tenant
        }

        // Final dynamic link
        return String.format(
                "%s/mesh-suite/v1.0/auth/%s/verify-account?email=%s",
                baseUrl,
                tenantId,
                email
        );
    }


    // 🔹 For privileged users only
    @Async("emailExecutor")
    public void sendTemporaryPasswordEmail(Users user, String tempPassword) {
        try {
            String content = buildTemporaryPasswordTemplate(user.getFirstName(), tempPassword);
            sendEmail(user.getEmail(), "Your Temporary Password - MESH", content, true);
        } catch (Exception e) {
            log.error("Failed to send temporary password email to {} - full error: ", user.getEmail(), e);

        }
    }

    // For non-privileged users only
    @Async("emailExecutor")
    public void sendVerificationLinkEmail(Users user, HttpServletRequest request) {
        try {
            String link = resolveVerificationLink(request, user.getEmail());
            String content = buildVerificationLinkTemplate(user.getFirstName(), link);
            sendEmail(user.getEmail(), "Activate Your Account - MESH", content, true);
        } catch (Exception e) {
            log.error("Failed to send verification link email to {} - {}", user.getEmail(), e.getMessage(), e);
        }
    }

    private void sendEmail(String toEmail, String subject, String content, boolean isHtml) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailFrom, mailFromName);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content, isHtml);
        mailSender.send(message);
        log.info("Email sent successfully to {}", toEmail);
    }


    @Async("emailExecutor")
    public void sendEmail(String recipientEmail, String subject, String body, boolean isHtml, String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, attachmentPath != null, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            // Add attachment if path is provided
            if (attachmentPath != null) {
                File file = new File(attachmentPath);
                if (file.exists()) {
                    FileSystemResource resource = new FileSystemResource(file);
                    helper.addAttachment(file.getName(), resource);
                    log.info("Attachment added: {}", file.getAbsolutePath());
                } else {
                    log.warn("Attachment file not found: {}", attachmentPath);
                }
            }

            mailSender.send(message);
            log.info("Email sent successfully to {}", recipientEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", recipientEmail, e.getMessage(), e);

        }
    }


    @Async("emailExecutor")
    public void sendFormAssignmentEmail(String companyAdminFirstName, String formTitle, String assignDate, String recipientEmail) {
        try {
            String content = String.format("""
                    <html>
                      <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                        <h3 style="color: #2c3e50;">New Form Assigned</h3>
                        <p>Hello %s,</p>
                        <p>Your company has been assigned a new form in <strong>MESH</strong>.</p>
                        <ul>
                          <li><strong>Form Title:</strong> %s</li>
                          <li><strong>Assigned Date:</strong> %s</li>
                        </ul>
                        <p>Please log in to your dashboard to complete or review it.</p>
                        <p style="margin-top: 20px;">Best regards,<br><strong>The MESH Team</strong></p>
                      </body>
                    </html>
                    """, companyAdminFirstName, formTitle, assignDate);

            sendEmail(recipientEmail, "New Form Assigned - MESH", content, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send form assignment email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }


    @Async("emailExecutor")
    public void sendFormSubmissionEmail(String companyAdminFirstName,
                                        String formTitle,
                                        String clientName,
                                        String submissionDate,
                                        String recipientEmail) throws MessagingException, UnsupportedEncodingException {
        String content = String.format("""
        <html>
          <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; color: #333;">
            <div style="background-color: #f8f9fa; padding: 30px; border-radius: 8px;">
              <h2 style="color: #2c3e50; text-align: center;">New Form Submission</h2>
              <p>Hello %s,</p>
              <p>A client has submitted a form:</p>
              <ul>
                <li><strong>Form Title:</strong> %s</li>
                <li><strong>Submitted By:</strong> %s</li>
                <li><strong>Submission Date:</strong> %s</li>
              </ul>
              <p style="margin-top: 30px; color: #7f8c8d; font-size: 14px; text-align: center;">
                Best regards,<br><strong>The MESH Team</strong>
              </p>
            </div>
          </body>
        </html>
        """, companyAdminFirstName, formTitle, clientName, submissionDate);

        sendEmail(recipientEmail, "New Form Submission - MESH", content, true);
    }

    @Async("emailExecutor")
    public void sendFormResponseEmail(String formTitle, String linkToResponse, String recipientEmail) throws MessagingException, UnsupportedEncodingException {
        String content = String.format("""
        <html>
          <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; color: #333;">
            <div style="background-color: #f8f9fa; padding: 30px; border-radius: 8px;">
              <h2 style="color: #2c3e50; text-align: center;">Response Received</h2>
              <p>Hi there,</p>
              <p>Your form has received a response from our team. Please review it:</p>
              <ul>
                <li><strong>Form Title:</strong> %s</li>
                <li><strong>Link to Response:</strong> <a href="%s">%s</a></li>
              </ul>
              <p style="margin-top: 30px; color: #7f8c8d; font-size: 14px; text-align: center;">
                Best regards,<br><strong>The MESH Team</strong>
              </p>
            </div>
          </body>
        </html>
        """, formTitle, linkToResponse, linkToResponse);

        sendEmail(recipientEmail, "Response Received - MESH", content, true);
    }

    @Async("emailExecutor")
    public void sendDocumentIssuanceEmail(String companyName, String documentLink, String recipientEmail) throws MessagingException, UnsupportedEncodingException {
        String content = String.format("""
        <html>
          <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; color: #333;">
            <div style="background-color: #f8f9fa; padding: 30px; border-radius: 8px;">
              <h2 style="color: #2c3e50; text-align: center;">Document Issued</h2>
              <p>Hi there,</p>
              <p>A document has been issued to you by <strong>%s</strong>.</p>
              <p>You can view or download it here: <a href="%s">%s</a></p>
              <p style="margin-top: 30px; color: #7f8c8d; font-size: 14px; text-align: center;">
                Best regards,<br><strong>The MESH Team</strong>
              </p>
            </div>
          </body>
        </html>
        """, companyName, documentLink, documentLink);

        // Send directly from default email
        sendEmail(recipientEmail, "Document Issued - MESH", content, true);
    }



    // ===================== HTML TEMPLATES =====================

    private String buildTemporaryPasswordTemplate(String name, String tempPassword) {
        return """
        <html>
          <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; color: #333;">
            <div style="background-color: #f8f9fa; padding: 30px; border-radius: 8px;">
              <h2 style="color: #2c3e50; text-align: center;">Welcome to MESH</h2>
              <p>Dear %s,</p>
              <p>Your privileged account has been created successfully. Use the temporary password below to log in:</p>
              <div style="background-color: #ffffff; border: 1px solid #e1e1e1; border-radius: 6px; padding: 20px; text-align: center; font-size: 22px; font-weight: bold; letter-spacing: 2px;">%s</div>
              <p>We recommend changing your password immediately after logging in.</p>
              <p style="margin-top: 30px; color: #7f8c8d; font-size: 14px; text-align: center;">Best regards,<br><strong>The MESH Team</strong></p>
            </div>
          </body>
        </html>
        """.formatted(name, tempPassword);
    }

    private String buildVerificationLinkTemplate(String name, String link) {
        return """
        <html>
          <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 600px; margin: 0 auto; color: #333;">
            <div style="background-color: #f8f9fa; padding: 30px; border-radius: 8px;">
              <h2 style="color: #2c3e50; text-align: center;">Activate Your Account</h2>
              <p>Dear %s,</p>
              <p>Thank you for joining <strong>MESH</strong>. Please click the button below to activate your account:</p>
              <a href="%s" style="display: inline-block; background-color: #007bff; color: white; text-decoration: none; border-radius: 6px; padding: 10px 20px; margin: 10px 0; font-weight: bold;">Activate Account</a>
              <p>If you did not request this account, please ignore this email.</p>
              <p style="margin-top: 30px; color: #7f8c8d; font-size: 14px; text-align: center;">Best regards,<br><strong>The MESH Team</strong></p>
            </div>
          </body>
        </html>
        """.formatted(name, link);
    }
}