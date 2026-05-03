package com.codebit.api.repository;

import com.codebit.api.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByAboutIdOrderByCategoryAscSortOrderAsc(Long aboutId);

    List<Skill> findByAboutIdAndCategoryOrderBySortOrderAsc(Long aboutId, String category);

    List<Skill> findByAboutIdAndProficiencyGreaterThanEqualOrderByProficiencyDesc(Long aboutId, Integer proficiency);

    @Query("SELECT DISTINCT s.category FROM Skill s WHERE s.about.id = :aboutId AND s.category IS NOT NULL")
    List<String> findDistinctCategories(@Param("aboutId") Long aboutId);

    @Query("SELECT s.category, COUNT(s) FROM Skill s WHERE s.about.id = :aboutId GROUP BY s.category")
    List<Object[]> countByCategory(@Param("aboutId") Long aboutId);

    @Modifying
    @Query("DELETE FROM Skill s WHERE s.about.id = :aboutId")
    void deleteByAboutId(@Param("aboutId") Long aboutId);

    Optional<Skill> findByAboutIdAndName(Long aboutId, String name);

    @Query("SELECT MAX(s.sortOrder) FROM Skill s WHERE s.about.id = :aboutId")
    Integer findMaxSortOrder(@Param("aboutId") Long aboutId);
}