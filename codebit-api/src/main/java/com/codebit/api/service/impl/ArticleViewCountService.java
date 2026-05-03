package com.codebit.api.service.impl;

import com.codebit.api.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/11 星期六
 * @Description:
 * @VERSON: 17
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleViewCountService {


    private final ArticleRepository articleRepository;

    @Async  // 异步执行
    public void incrementViewCountAsync(Long articleId) {
        try {
            // 方式1：使用原生更新（推荐，避免事务和乐观锁问题）
            articleRepository.incrementViewCount(articleId);
            log.info("异步更新阅读量成功: articleId={}", articleId);
        } catch (Exception e) {
            log.error("异步更新阅读量失败: articleId={}", articleId, e);
        }
    }
}
