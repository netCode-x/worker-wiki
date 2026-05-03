package com.codebit.api.repository;

import com.codebit.api.entity.AboutHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AboutHistoryRepository extends JpaRepository<AboutHistory, Long> {

    /**
     * 查询指定页面的所有历史版本（按版本倒序）
     */
    List<AboutHistory> findByAboutIdOrderByVersionDesc(Long aboutId);

    /**
     * 查询指定版本
     */
    Optional<AboutHistory> findByAboutIdAndVersion(Long aboutId, Integer version);

    /**
     * 获取最大版本号
     */
    @Query("SELECT MAX(h.version) FROM AboutHistory h WHERE h.aboutId = :aboutId")
    Integer findMaxVersionByAboutId(@Param("aboutId") Long aboutId);

    /**
     * 删除指定页面的所有历史（当删除页面时）
     */
    @Modifying
    @Query("DELETE FROM AboutHistory h WHERE h.aboutId = :aboutId")
    void deleteByAboutId(@Param("aboutId") Long aboutId);

    /**
     * 清理旧版本，保留最近N个版本
     */
    @Modifying
    @Query(value = "DELETE FROM about_history WHERE about_id = :aboutId AND version NOT IN " +
            "(SELECT version FROM (SELECT version FROM about_history WHERE about_id = :aboutId " +
            "ORDER BY version DESC LIMIT :keepCount) AS tmp)", nativeQuery = true)
    void cleanOldVersions(@Param("aboutId") Long aboutId, @Param("keepCount") int keepCount);
}