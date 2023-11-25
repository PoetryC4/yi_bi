package com.yibi.backend.service.impl;

import com.yibi.backend.service.EmailService;
import com.yibi.backend.utils.EmailCodeUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class EmailServiceImpl implements EmailService {
    @Resource
    private JavaMailSender mailSender;


    public void sendVerificationCode(String toEmail) {
        String verificationCode = EmailCodeUtils.generateVerificationCode(toEmail);
        // 设置验证码有效时间为300秒

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("473240934@qq.com");
        message.setTo(toEmail);
        message.setSubject("验证码-yi_bi");
        message.setText("您的验证码是：" + verificationCode + "，有效时间为300秒。");

        mailSender.send(message);
    }
}
