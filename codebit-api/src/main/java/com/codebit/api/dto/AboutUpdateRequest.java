package com.codebit.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AboutUpdateRequest {
    private String title;
    private String subtitle;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String avatarUrl;
    private String coverImageUrl;
    private String email;
    private String location;
    private Integer status;
    private String changeReason;
    private List<SocialLinkDto> socialLinks;
    private List<SkillDto> skills;
    private List<FootprintDto> footprints;
}