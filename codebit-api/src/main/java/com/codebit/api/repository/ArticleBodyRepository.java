package com.codebit.api.repository;

import com.codebit.api.entity.ArticleBody;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/9 星期四
 * @Description:
 * @VERSON: 17
 */
public interface ArticleBodyRepository extends CrudRepository<ArticleBody, Long> {

    /**
     * 根据文章ID查询内容
     */
    Optional<ArticleBody> findByArticleId(Long articleId);

}
