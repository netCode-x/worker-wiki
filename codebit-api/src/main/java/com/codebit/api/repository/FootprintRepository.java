package com.codebit.api.repository;

import com.codebit.api.entity.Footprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FootprintRepository extends JpaRepository<Footprint, Long> {

    List<Footprint> findByAboutIdOrderByYearDescSortOrderAsc(Long aboutId);

    List<Footprint> findByAboutIdOrderByYearDesc(Long aboutId);

    List<Footprint> findByAboutIdAndIsHighlightOrderByYearDescSortOrderAsc(Long aboutId, Integer isHighlight);

    List<Footprint> findByAboutIdAndYearOrderBySortOrderAsc(Long aboutId, Integer year);

    List<Footprint> findByAboutIdAndYearBetweenOrderByYearDescSortOrderAsc(Long aboutId, Integer startYear, Integer endYear);

    @Query("SELECT f.year, COUNT(f) FROM Footprint f WHERE f.about.id = :aboutId GROUP BY f.year ORDER BY f.year DESC")
    List<Object[]> countByYear(@Param("aboutId") Long aboutId);

    @Query("SELECT MAX(f.year) FROM Footprint f WHERE f.about.id = :aboutId")
    Integer findMaxYear(@Param("aboutId") Long aboutId);

    @Query("SELECT MIN(f.year) FROM Footprint f WHERE f.about.id = :aboutId")
    Integer findMinYear(@Param("aboutId") Long aboutId);

    @Modifying
    @Query("DELETE FROM Footprint f WHERE f.about.id = :aboutId")
    void deleteByAboutId(@Param("aboutId") Long aboutId);

    @Modifying
    @Query("UPDATE Footprint f SET f.isHighlight = :isHighlight WHERE f.about.id = :aboutId AND f.id IN :ids")
    void batchUpdateHighlight(@Param("aboutId") Long aboutId,
                              @Param("ids") List<Long> ids,
                              @Param("isHighlight") Integer isHighlight);

    List<Footprint> findTop5ByAboutIdOrderByYearDescSortOrderAsc(Long aboutId);
}