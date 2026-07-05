package com.codebit.api.dto.articleDto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;       // 原始 Markdown
    private String contentHtml;   // 转换后的 HTML
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private Integer commentCounts;
    private Integer viewCounts;
    private Integer weigth;
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}