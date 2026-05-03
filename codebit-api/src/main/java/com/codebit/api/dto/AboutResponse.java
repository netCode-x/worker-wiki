package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AboutResponse {
    private String title;
    private String subtitle;
    private String contentHtml;
    private String avatarUrl;
    private String coverImageUrl;
    private String email;
    private String location;
    private List<SocialLinkPublicDto> socialLinks;
    private List<SkillPublicDto> skills;
    private List<FootprintPublicDto> footprints;
    private Long viewCount;
    private Long likeCount;
    private String buildDays;
    private String lastUpdated;
}