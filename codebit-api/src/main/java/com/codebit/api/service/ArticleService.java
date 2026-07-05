package com.codebit.api.service;

import com.codebit.api.dto.*;
import com.codebit.api.dto.articleDto.*;

public interface ArticleService {


    ArticleResponse createArticle(ArticleCreateRequest req, Long authorId);


    ArticleResponse updateArticle(ArticleUpdateRequest req, Long authorId);


    ArticleResponse getArticleById(Long articleId);

    PageResult<ArticleListResponse> getArticleList(ArticleQueryRequest request);

    String getArticleHtmlById(Long id);

    String previewMarkdown(String markdown);

    void deleteArticle(Long id, Long authorId);

}