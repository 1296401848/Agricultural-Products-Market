package com.wyt.agriculture.service.impl;

import com.wyt.agriculture.service.IMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
@Slf4j
public class MailServiceImpl implements IMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("农产品商城 - 密码重置验证码");

            String content = "<div style='padding: 20px; background: #f5f7fa;'>"
                    + "<div style='max-width: 500px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px;'>"
                    + "<h2 style='color: #409eff; text-align: center;'>农产品商城</h2>"
                    + "<p style='font-size: 16px; color: #303133;'>您正在申请密码重置，验证码如下：</p>"
                    + "<div style='text-align: center; padding: 20px; background: #f0f9ff; border-radius: 8px; margin: 20px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; color: #409eff; letter-spacing: 8px;'>" + code + "</span>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #909399;'>验证码 5 分钟内有效，请勿泄露给他人。</p>"
                    + "<p style='font-size: 14px; color: #909399;'>如果这不是您的操作，请忽略此邮件。</p>"
                    + "</div></div>";

            helper.setText(content, true);
            mailSender.send(message);

            log.info("验证码邮件发送成功: to={}", to);
        } catch (MessagingException e) {
            log.error("验证码邮件发送失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }
}
