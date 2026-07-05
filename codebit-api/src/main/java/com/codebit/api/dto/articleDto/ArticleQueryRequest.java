package com.codebit.api.dto.articleDto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ArticleQueryRequest {

    /**
     * 搜索关键词（标题或简介模糊匹配）
     */
    private String keyword;

    /**
     * 分类ID筛选
     */
    private Long categoryId;

    /**
     * 作者ID筛选
     */
    private Long authorId;

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小为1")
    private Integer pageSize = 10;
}