package com.codebit.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 关于页面历史版本实体类
 */
@Entity
@Table(name = "about_history", indexes = {
        @Index(name = "idx_about_version", columnList = "about_id, version"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AboutHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的关于页面ID
     */
    @Column(name = "about_id", nullable = false)
    private Long aboutId;

    /**
     * Markdown内容
     */
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    /**
     * 渲染后的HTML
     */
    @Column(columnDefinition = "LONGTEXT")
    private String contentHtml;

    /**
     * 版本号
     */
    @Column(nullable = false)
    private Integer version;

    /**
     * 社交链接快照（JSON格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String socialLinksSnapshot;

    /**
     * 技能快照（JSON格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String skillsSnapshot;

    /**
     * 足迹快照（JSON格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String footprintsSnapshot;

    /**
     * 修改人
     */
    @Column(length = 50)
    private String changedBy;

    /**
     * 修改原因
     */
    @Column(length = 200)
    private String changeReason;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (version == null) {
            version = 1;
        }
    }
}