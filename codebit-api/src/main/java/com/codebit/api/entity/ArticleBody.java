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
@Table(name = "ms_article_body")
@EntityListeners(AuditingEntityListener.class)
public class ArticleBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("内容ID")
    private Long id;

    @Column(name = "article_id", nullable = false, unique = true)
    @Comment("关联的文章ID")
    private Long articleId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    @Comment("原始 Markdown 内容")
    private String content;

    @Lob
    @Column(name = "content_html", columnDefinition = "LONGTEXT")
    @Comment("转换后的 HTML 内容")
    private String contentHtml;

    @CreatedDate
    @Column(name = "create_date", updatable = false)
    @Comment("创建时间")
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "update_date")
    @Comment("更新时间")
    private LocalDateTime updateDate;
}