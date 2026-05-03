package com.codebit.api.repository;

import com.codebit.api.entity.AboutPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AboutRepository  extends JpaRepository<AboutPage, Long> {


    Optional<AboutPage> findFirstByStatusOrderByUpdatedAtDesc(Integer status);

    @Modifying
    @Query("UPDATE AboutPage a  set a.viewCount = a.viewCount +1 where  a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("update AboutPage a set a.likeCount = a.likeCount +1 where  a.id = :id")
    void incrementLikeCount(@Param("id") Long id);


}
