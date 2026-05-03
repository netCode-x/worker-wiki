package com.codebit.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 个人足迹/时间轴实体类
 */
@Entity
@Table(name = "footprints", indexes = {
        @Index(name = "idx_about_year", columnList = "about_id, year DESC, sort_order"),
        @Index(name = "idx_about_highlight", columnList = "about_id, is_highlight")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Footprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的关于页面
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "about_id", nullable = false)
    private AboutPage about;

    /**
     * 年份（必填）
     */
    @Column(nullable = false)
    private Integer year;

    /**
     * 月份（1-12，可选）
     */
    private Integer month;

    /**
     * 事件标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 详细描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 地点
     */
    @Column(length = 100)
    private String location;

    /**
     * 图标（emoji或图标类名）
     */
    @Column(length = 50)
    private String icon;

    /**
     * 配图URL
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * 是否高亮展示（重要事件）
     */
    private Integer isHighlight = 0;

    /**
     * 排序序号（同一年内）
     */
    private Integer sortOrder = 0;

    /**
     * 链接地址（可选，如项目链接、文章链接）
     */
    @Column(length = 500)
    private String linkUrl;

    /**
     * 链接文字
     */
    @Column(length = 50)
    private String linkText;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (isHighlight == null) {
            isHighlight = 0;
        }
    }
}