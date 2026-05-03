package com.codebit.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Comment;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ms_article")
@EntityListeners(AuditingEntityListener.class)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("文章ID")
    private Long id;


    @Column(nullable = true, length = 20)
    @Comment("分类")
    private String subCategory;

    @Column(nullable = false, length = 100)
    @Comment("标题")
    private String title;

    @Column(length = 255)
    @Comment("简介")
    private String summary;

    @Column(name = "category_id")
    @Comment("分类ID")
    private Long categoryId;

    @Column(name = "author_id")
    @Comment("作者ID")
    private Long authorId;

    @Column(name = "article_Id")
    @Comment("文章ID")
    private Long articleId;

    @Column(name = "comment_counts", columnDefinition = "int default 0")
    @Comment("评论数量")
    private Integer commentCounts;

    @Column(name = "view_counts", columnDefinition = "int default 0")
    @Comment("浏览数量")
    private Integer viewCounts;

    @Column(columnDefinition = "int default 0")
    @Comment("权重/置顶")
    private Integer weigth;

    @Column(nullable = false)
    @Comment("状态: 0-草稿, 1-已发布, 2-已删除")
    private Integer status;

    @CreatedDate
    @Column(name = "create_date", updatable = false)
    @Comment("创建时间")
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "update_date")
    @Comment("更新时间")
    private LocalDateTime updateDate;

    // 关联内容（非持久化字段，仅用于业务层）
    @Transient
    private ArticleBody articleBody;
}