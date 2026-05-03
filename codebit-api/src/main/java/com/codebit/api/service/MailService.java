package com.codebit.api.service;

public interface MailService {

    void sendVerificationCode(String toEmail, String code);
}
