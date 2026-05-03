package com.codebit.api.service.impl;

import com.codebit.api.dto.ForgetPasswordRequest;
import com.codebit.api.dto.ResetPasswordRequest;
import com.codebit.api.entity.User;
import com.codebit.api.repository.UserRepository;
import com.codebit.api.service.MailService;
import com.codebit.api.service.PasswordResetService;
import com.codebit.api.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final RedisScript<Long> checkAndDeleteScript;

    @Value("${spring.redis.reset-code.expire-minuter:5}")
    private int getCodeExpireMinutes;


    private static final String RESET_CODE_PREFIX = "reset:code:";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public void sendResetCode(ForgetPasswordRequest request) {
        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(404, "该邮箱未注册"));

        String rateLimitKey = RESET_CODE_PREFIX + "rate:" + email;
        Boolean isFirstRequest = redisService.setIfAbsent(
                rateLimitKey,
                System.currentTimeMillis(),
                60,
                TimeUnit.SECONDS
        );
        if (Boolean.FALSE.equals(isFirstRequest)) {
            // 获取最后时间
            Long expireSecondes = redisService.getExpire(rateLimitKey, TimeUnit.SECONDS);

            throw new BusinessException(429, String.format("发送过于频繁，请稍后%d秒后重试", expireSecondes));
        }

        String code = generateVerificationCode();
        String redisKey = RESET_CODE_PREFIX + email;
        redisService.set(redisKey, code, getCodeExpireMinutes, TimeUnit.MINUTES);

        try {
            mailService.sendVerificationCode(email, code);
            log.info("验证码发送成功: email={}", email);
        } catch (Exception e) {
            redisService.delete(redisKey);
            throw new BusinessException(500, "邮件发送失败，请稍后重试");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getEmailCode();
        String newPassword = request.getNewPassword();

        String redisKey = RESET_CODE_PREFIX + email;

        Long result = redisService.execute(checkAndDeleteScript, Collections.singletonList(redisKey), code);

        if (result ==null || result ==-1){
            throw new BusinessException(400,"验证码已过期");
        }
        if (result ==0){
            throw  new BusinessException(400,"验证码错误");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisService.delete(redisKey);

        log.info("密码重置成功: email={}", email);
    }


    private String generateVerificationCode() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }


}