package com.codebit.api.controller;

import com.codebit.api.config.CustomUserDetails;
import com.codebit.api.dto.*;
import com.codebit.api.dto.articleDto.*;
import com.codebit.api.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
* @Auther: yangkaihu
* @Date: 2026/4/10 星期五
* @Description: 
* @VERSON: 17
*/
@Slf4j
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@Tag(name = "文章接口")
public class ArticleController {


    private final ArticleService articleService;


    /**
     * 获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            return userDetails.getUserId();
        }
        throw new RuntimeException("用户未登录");
    }


    /**
     * 创建文章
     * POST /api/articles/create
     */
    @RequestMapping(path = "/create",method = RequestMethod.POST)
    @Operation(summary = "创建文章")
    public Result<ArticleResponse> createArticle(@Valid @RequestBody ArticleCreateRequest request) {
        Long authorId = getCurrentUserId();
        log.info("创建文章: title={}, authorId={}", request.getTitle(), authorId);
        ArticleResponse response = articleService.createArticle(request, authorId);
        return Result.success(response);
    }


    /**
     * 更新文章
     * @param request
     * @return
     */
    @RequestMapping(path = "/update",method = RequestMethod.PUT)
    @Operation(summary = "更新文章")
    public Result<ArticleResponse> updateArticle(@Valid @RequestBody ArticleUpdateRequest request) {
        Long authorId = getCurrentUserId();
        log.info("更新文章： id={}，authorId={}", request.getId(), authorId);
        ArticleResponse re = articleService.updateArticle(request, authorId);
        return Result.success(re);
    }


    /**
     * 获取文章详情（完整信息）
     * @param id
     * @return
     */
    @RequestMapping(path = "/{id}",method = RequestMethod.GET)
    @Operation(summary = "获取文章详情（完整信息）")
    public Result<ArticleResponse> getArticle(@PathVariable  Long id) {
            log.info("获取文章详情： id={}", id);
        ArticleResponse reponse = articleService.getArticleById(id);

        return Result.success(reponse);
    }

    /**
     * 获取文章html 内容（渲染）
     * @param id
     * @return
     */
    @RequestMapping(path = "/{id}/html",method = RequestMethod.GET)
    @Operation(summary = "获取文章html 内容（渲染）")
    public Result<String> getArticleHtml(@PathVariable  Long id) {
        log.info("获取文章html: id={}", id);
        String html = articleService.getArticleHtmlById(id);
        return Result.success(html);
    }


    /**
     * 预览Markdown （实时抓换）
     * @param markdown
     * @return
     */
    @RequestMapping(path = "/preview",method = RequestMethod.POST)
    @Operation(summary = "预览Markdown （实时抓换）")
    public  Result<String> preViewMarkdown(@RequestBody String markdown) {
        log.info("预览Markdown: length={}", markdown !=null ? markdown.length() : 0);

        String html = articleService.previewMarkdown(markdown);
        return Result.success(html);

    }
    /**
     * 分页查询文章列表
     * GET /api/articles?keyword=xxx&categoryId=1&pageNum=1&pageSize=10
     */
    @RequestMapping(path = "/page", method = RequestMethod.GET)
    @Operation(summary = "分页查询文章列表")
    public Result<PageResult<ArticleListResponse>> getArticleList(@Valid ArticleQueryRequest request) {
        PageResult<ArticleListResponse> result = articleService.getArticleList(request);
        return Result.success(result);
    }

    /**
     * 获取当前用户的所有文章（用于个人中心）
     * GET /api/articles/my
     */
    @RequestMapping(path = "/my",method = RequestMethod.GET)
    @Operation(summary = "获取当前用户的所有文章（用于个人中心）")
    public Result<PageResult<ArticleListResponse>> getMyArticles(@Valid ArticleQueryRequest request) {
        Long authorId = getCurrentUserId();
        request.setAuthorId(authorId);

        log.info("查询我的文章: authorId={}, pageNum={}, pageSize={}",
                authorId, request.getPageNum(), request.getPageSize());

        PageResult<ArticleListResponse> result = articleService.getArticleList(request);
        return Result.success(result);
    }

  /**
     * 删除文章（软删除）
     * DELETE /api/articles/{id}
     */
    @RequestMapping(path = "/{id}/delete",method = RequestMethod.DELETE)
    @Operation(summary = " 软删除文章")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        Long authorId = getCurrentUserId();
        log.info("删除文章: id={}, authorId={}", id, authorId);
        articleService.deleteArticle(id, authorId);
        return Result.success(null);
    }

}
