package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AboutAdminResponse {
    private String title;
    private String subtitle;
    private String content;
    private String avatarUrl;
    private String coverImageUrl;
    private String email;
    private String location;
    private Integer status;
    private List<SocialLinkDto> socialLinks;
    private List<SkillDto> skills;
    private List<FootprintDto> footprints;
    private Integer version;
    private LocalDateTime updatedAt;
    private String updatedBy;
}