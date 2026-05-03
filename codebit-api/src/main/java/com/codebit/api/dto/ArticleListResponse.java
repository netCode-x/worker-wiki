package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ArticleListResponse {
    private Long id;
    private String title;
    private String summary;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private Long articleId;
    private Integer commentCounts;
    private Integer viewCounts;
    private String content;
    private String contentHtml;
    private Integer weigth;
    private LocalDateTime createDate;
}