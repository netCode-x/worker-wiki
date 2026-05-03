package com.codebit.api.service.impl;


import com.codebit.api.converter.BuildResponseConverter;
import com.codebit.api.dto.*;
import com.codebit.api.entity.Article;
import com.codebit.api.entity.ArticleBody;
import com.codebit.api.entity.User;
import com.codebit.api.repository.ArticleBodyRepository;
import com.codebit.api.repository.ArticleRepository;
import com.codebit.api.repository.UserRepository;
import com.codebit.api.service.ArticleService;
import com.codebit.api.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/5 星期日
 * @Description: 文章业务处理
 * @VERSON: 17
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArticleServiceImpl implements ArticleService {


    private final ArticleRepository articleRepository;
    private final BuildResponseConverter articleConverter;
    private final ArticleBodyRepository articleBodyRepository;
    private final UserRepository userRepository;
    private final MarkdownService markdownService;
    private final ArticleViewCountService articleViewCountService;


    /**
     * 创建文章
     *
     * @param req
     * @param authorId
     * @return
     */
    @Override
    @Transactional
    public ArticleResponse createArticle(ArticleCreateRequest req, Long authorId) {
        String contentHtml = markdownService.toHtml(req.getContent());

        String summary = req.getSummary();
        if (summary != null || summary.trim().isEmpty()) {
            summary = markdownService.toPlainText(req.getContent(), 150);
        }
        Article article = Article.builder()
                .title(req.getTitle())
                .summary(req.getSummary())
                .categoryId(req.getCategoryId())
                .authorId(authorId)
                .status(req.getStatus() != null ? req.getStatus() : 1)
                .commentCounts(0)
                .viewCounts(0)
                .weigth(0)
                .build();

        log.info("save article ");
        Article savedArticle = articleRepository.save(article);

        ArticleBody articleBody = ArticleBody.builder()
                .articleId(savedArticle.getId())
                .content(req.getContent())
                .contentHtml(contentHtml)
                .build();

        articleBodyRepository.save(articleBody);

        savedArticle.setArticleBody(articleBody);
        log.info("文章创建成功: id={}, title={}", savedArticle.getId(), savedArticle.getTitle());

        User author = userRepository.findById(authorId)
                .orElse(null);

        return articleConverter.ArticleConverterResponse(savedArticle, articleBody, author);
    }


    /**
     * 更新文章
     */
    @Override
    @Transactional
    public ArticleResponse updateArticle(ArticleUpdateRequest req, Long authorId) {

        Article article = articleRepository.findById(req.getId())
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));

        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(403, "无权限修改此文章");
        }
        if (req.getTitle() != null) {
            article.setTitle(req.getTitle());
        }
        if (req.getCategoryId() != null) {
            article.setCategoryId(req.getCategoryId());
        }

        if (req.getStatus() != null) {
            article.setStatus(req.getStatus());
        }
        articleRepository.save(article);
        // 4. 更新内容表
        ArticleBody articleBody = articleBodyRepository.findByArticleId(article.getId())
                .orElseThrow(() -> new BusinessException(404, "文章内容不存在"));

        if (req.getContent() != null) {
            articleBody.setContent(req.getContent());
            articleBody.setContentHtml(markdownService.toHtml(req.getContent()));

            // 如果简介未手动指定，则自动更新
            if (req.getSummary() == null) {
                article.setSummary(markdownService.toPlainText(req.getContent(), 150));
                articleRepository.save(article);
            }
        }

        if (req.getSummary() != null) {
            article.setSummary(req.getSummary());
            articleRepository.save(article);
        }

        articleBodyRepository.save(articleBody);

        log.info("文章更新成功: id={}, title={}", article.getId(), article.getTitle());

        // 5. 获取作者信息
        User author = userRepository.findById(article.getAuthorId()).orElse(null);

        return articleConverter.ArticleConverterResponse(article, articleBody, author);

    }

    /**
     * 获取文章详情（包含内容）
     */
    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getArticleById(Long articleId) {

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));

        if (article.getStatus() != 1) {
            throw new BusinessException(404, "文章不存在或未发布");
        }
        ArticleBody articleBody = articleBodyRepository.findByArticleId(articleId)
                .orElseThrow(() -> new BusinessException(404, "文章内容不存在"));

        articleViewCountService.incrementViewCountAsync(articleId);

        articleRepository.save(article);

        User author = userRepository.findById(article.getAuthorId())
                .orElse(null);

        return articleConverter.ArticleConverterResponse(article, articleBody, author);

    }

    /**
     * 获取文章列表（不含内容，性能优化）
     *
     * @param request
     * @return
     */
    public PageResult<ArticleListResponse> getArticleList(ArticleQueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "weigth", "createDate");

        PageRequest pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize(), sort);

        Page<Article> articlePage;
        articlePage = articleRepository.findByStatusOrderByCreateDateDesc(1, pageable);
        // 批量查询作者信息
        List<Long> authorIds = articlePage.getContent().stream()
                .map(Article::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<ArticleListResponse> responseList = articlePage.getContent().stream()
                .map(article -> {
                    User author = userMap.get(article.getAuthorId());
                    return ArticleListResponse.builder()
                            .id(article.getId())
                            .title(article.getTitle())
                            .summary(article.getSummary())
                            .categoryId(article.getCategoryId())
                            .authorId(article.getAuthorId())
                            .articleId(article.getArticleId())
                            .authorName(author != null ? author.getNickName() : "未知用户")
                            .commentCounts(article.getCommentCounts())
                            .viewCounts(article.getViewCounts())
                            .weigth(article.getWeigth())
                            .createDate(article.getCreateDate())
                            .build();

                })
                .collect(Collectors.toList());

        return new PageResult<>(
                articlePage.getTotalElements(),
                request.getPageNum(),
                request.getPageSize(),
                articlePage.getTotalPages(),
                responseList
        );
    }


    /**
     * 获取文章 HTML 内容（仅用于渲染）
     */
    @Override
    @Transactional(readOnly = true)
    public String getArticleHtmlById(Long id) {
        ArticleBody articleBody = articleBodyRepository.findByArticleId(id)
                .orElseThrow(() -> new BusinessException(404, "文章内容不存在"));
        return articleBody.getContentHtml();
    }

    /**
     * 预览 Markdown
     */
    @Override
    public String previewMarkdown(String markdown) {
        return markdownService.toHtml(markdown);
    }


    @Transactional
    @Override
    public void deleteArticle(Long id, Long authorId) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));


        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(403, "无权限删除此文章");
        }

        article.setStatus(2);
        articleRepository.save(article);
        log.info("文章删除成功：: id={}, title={}", id, article.getTitle());

    }

}
