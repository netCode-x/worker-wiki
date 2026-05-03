package com.codebit.api.repository;


import com.codebit.api.entity.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    List<SocialLink> findByAboutIdAndEnabledOrderBySortOrderAsc(Long aboutId, Integer enabled);

    List<SocialLink> findByAboutIdOrderBySortOrderAsc(Long aboutId);

    List<SocialLink> findByAboutIdAndPlatform(Long aboutId, String platform);

    @Modifying
    @Query("DELETE FROM SocialLink s WHERE s.about.id = :aboutId")
    void deleteByAboutId(@Param("aboutId") Long aboutId);

    @Modifying
    @Query("UPDATE SocialLink s SET s.enabled = :enabled WHERE s.about.id = :aboutId")
    void batchUpdateEnabled(@Param("aboutId") Long aboutId, @Param("enabled") Integer enabled);
}