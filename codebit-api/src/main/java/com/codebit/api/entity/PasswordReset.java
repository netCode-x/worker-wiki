package com.codebit.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ms_password_reset")
@EntityListeners(AuditingEntityListener.class)
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;

    @Column(nullable = false)
    private Boolean used = false;

    @CreatedDate
    @Column(name = "create_date", updatable = false)
    private LocalDateTime createDate;
}