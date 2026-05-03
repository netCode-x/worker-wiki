package com.codebit.api.service.impl;

import com.codebit.api.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceIpml implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        // HTML模板
        String htmlContent = String.format("""
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
                <div style="background-color: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    <h2 style="color: #333; margin-top: 0;">密码重置验证码</h2>
                    <p style="color: #666; line-height: 1.6;">您好！</p>
                    <p style="color: #666; line-height: 1.6;">您正在进行密码重置操作，验证码为：</p>
                    <div style="background-color: #f0f0f0; border-radius: 4px; padding: 15px; margin: 20px 0; text-align: center;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 4px; color: #1890ff;">%s</span>
                    </div>
                    <p style="color: #666; line-height: 1.6;">验证码有效期为<strong style="color: #ff4d4f;">5分钟</strong>，请勿泄露。</p>
                    <p style="color: #999; line-height: 1.6; font-size: 12px; border-top: 1px solid #eee; padding-top: 20px; margin-bottom: 0;">
                        如非本人操作，请忽略此邮件。
                    </p>
                </div>
            </div>
            """, code);

        try {
            // 使用 MimeMessage 支持 HTML
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【个人博客】密码重置验证码");
            message.setText(htmlContent, true);  // true 表示 HTML 格式

            mailSender.send(mimeMessage);
            log.info("验证码邮件发送成功: toEmail={}", toEmail);

        } catch (MessagingException e) {
            log.error("验证码邮件发送失败: toEmail={}, error={}", toEmail, e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }
}