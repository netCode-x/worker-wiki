package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class AboutStatisticsResponse {
    private Long totalViews;
    private Long totalLikes;
    private Integer totalSkills;
    private Integer totalSocialLinks;
    private Integer totalFootprints;
    private Integer versionCount;
    private Integer currentVersion;
    private Long totalViewCount;
    private Long totalLikeCount;
    private int totalVersions;
    private Map<Integer, Long> footprintsByYear;
    private Map<Integer, Long> footprintYearStats;
    private Map<String, Long> skillsByCategory;
    private Map<String, Long> skillCategoryStats;
    private LocalDateTime lastUpdated;
}