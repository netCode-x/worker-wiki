package com.codebit.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description: entity 实体类对应数据库的条数
 * @VERSON: 17
 */

@Data
@AllArgsConstructor
@Entity
@Table(name = "ms_user")
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * 注解ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("注解ID")
    private Integer id;

    /**
     * 用户名
     */
    @NotBlank
    @Column(unique = true, nullable = false) // 唯一约束，确保用户名不重复，数据库层面保证唯一性
    @Comment("用户名")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank
    @Column(nullable = false)
    @Comment("用户密码")
    private String password;

    /**
     * 昵称
     */

    @Column(name = "nick_name")
    @Comment("昵称")
    private String nickName;


    /**
     * 邮箱
     */
    @Column(length = 128, unique = true)
    @Comment("邮箱")
    private String email;

    @Column(name = "email_verified", columnDefinition = "tinyint(1) default 0")
    @Comment("邮箱是否已验证")
    private Boolean emailVerified;

    /**
     * 创建时间
     */

    @CreatedDate
    @Column(nullable = true)
    @Comment("创建时间")
    private LocalDateTime createDate;

    /**
     * 最后修改日期
     */

    @LastModifiedDate
    @Column(nullable = true)
    @Comment("最后修改日期")
    private LocalDateTime lastLoginDate;

    public User(){

    }

    public User(String username, String encodePassword) {
        this.username = username;
        this.password = encodePassword;
    }
    public User(String username, String encodePassword,String email) {
        this.username = username;
        this.password = encodePassword;
        this.email=email;
    }
}
