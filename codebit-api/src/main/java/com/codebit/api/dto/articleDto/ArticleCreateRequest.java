package com.codebit.api.dto.articleDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleCreateRequest {

    /**
     * 文章标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 文章内容（原始 Markdown 格式）
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 文章简介（可选，不填则后端自动从内容中提取）
     */
    private String summary;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 文章状态：0-草稿，1-已发布（默认），2-已删除
     */
    private Integer status = 1;
}