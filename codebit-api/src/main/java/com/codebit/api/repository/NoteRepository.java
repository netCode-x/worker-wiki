package com.codebit.api.repository;

import com.codebit.api.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * 查询用户的随记（按创建时间倒序）
     */
    Page<Note> findByAuthorIdAndStatusOrderByCreateDateDesc(Long authorId, Integer status, Pageable pageable);

    /**
     * 查询公开的随记（所有用户可见）
     */
    Page<Note> findByIsPrivateFalseAndStatusOrderByCreateDateDesc(Integer status, Pageable pageable);

    /**
     * 搜索随记（内容包含关键词）
     */
    @Query("SELECT n FROM Note n WHERE n.authorId = :authorId" +
            " AND n.status = :status AND n.content " +
            "LIKE CONCAT('%', :keyword, '%') ORDER BY n.createDate DESC")
    Page<Note> searchByAuthorAndKeyword(@Param("authorId") Long authorId, @Param("status") Integer status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 搜索公开随记
     */
    @Query("SELECT n FROM Note n WHERE n.isPrivate = false AND n.status " +
            "= :status AND n.content LIKE CONCAT('%', :keyword, '%') ORDER BY n.createDate DESC")
    Page<Note> searchPublicByKeyword(@Param("status") Integer status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 搜索公开随记（不分页）
     */
    @Query("SELECT n FROM Note n WHERE n.isPrivate = false " +
            "AND n.status = :status AND n.content LIKE CONCAT('%', :keyword, '%') ORDER BY n.createDate DESC")
    List<Note> searchPublicByword(@Param("status") Integer status, @Param("keyword") String keyword);


    /**
     * 查询所有公开随记（不分页）
     * @param status
     * @return
     */
    List<Note> findByIsPrivateFalseAndStatusOrderByCreateDateDesc(Integer status);
}