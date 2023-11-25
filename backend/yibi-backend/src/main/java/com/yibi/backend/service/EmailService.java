package com.yibi.backend.service;

public interface EmailService {
    public void sendVerificationCode(String toEmail);
}
