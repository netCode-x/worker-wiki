package com.codebit.api.repository;

import com.codebit.api.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    /**
     * 查询指定邮箱未使用且未过期的验证码
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.email = :email " +
            "AND pr.code = :code AND pr.used = false AND pr.expireTime > :now")
    Optional<PasswordReset> findValidCode(@Param("email") String email, @Param("code") String code, @Param("now") LocalDateTime now);

    /**
     * 标记验证码为已使用
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordReset pr SET pr.used = true WHERE pr.email = :email AND pr.code = :code")
    void markAsUsed(@Param("email") String email, @Param("code") String code);

    /**
     * 清理过期的验证码
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordReset pr WHERE pr.expireTime < :now")
    int deleteExpiredCodes(@Param("now") LocalDateTime now);
}