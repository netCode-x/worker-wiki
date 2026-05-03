package com.codebit.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleUpdateRequest {

    /**
     * 文章ID（必填，用于指定要更新的文章）
     */
    @NotNull(message = "文章ID不能为空")
    private Long id;

    /**
     * 文章标题（可选，不传则不更新）
     */
    private String title;

    /**
     * 文章内容（可选，不传则不更新）
     */
    private String content;

    /**
     * 文章简介（可选，不传则自动从内容中提取）
     */
    private String summary;

    /**
     * 分类ID（可选，不传则不更新）
     */
    private Long categoryId;

    /**
     * 文章状态：0-草稿，1-已发布，2-已删除（可选）
     */
    private Integer status;
}