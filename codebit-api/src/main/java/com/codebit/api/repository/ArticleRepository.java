package com.codebit.api.repository;

import com.codebit.api.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * 分页查询已发布的文章（不包含内容，用于列表页）
     */
    Page<Article> findByStatusOrderByCreateDateDesc(Integer status, Pageable pageable);

    /**
     * 根据分类ID查询
     */
    Page<Article> findByCategoryIdAndStatusOrderByCreateDateDesc(Long categoryId, Integer status, Pageable pageable);

    /**
     * 根据作者ID查询
     */
    Page<Article> findByAuthorIdAndStatusOrderByCreateDateDesc(Long authorId, Integer status, Pageable pageable);

    /**
     * 关键词搜索（只返回主表字段）
     */
    @Query("SELECT a FROM Article a WHERE a.status = :status " +
            "AND (a.title LIKE CONCAT('%', :keyword, '%')" +
            " OR a.summary LIKE CONCAT('%', :keyword, '%'))" +
            "ORDER BY a.weigth DESC, a.createDate DESC")
    Page<Article> searchByKeyword(@Param("status") Integer status, @Param("keyword") String keyword, Pageable pageable);


    /**
     * 原子性增加阅读量（避免并发问题）
     */
    @Modifying
    @Transactional
    @Query("UPDATE Article a SET a.viewCounts = a.viewCounts + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

}