package com.codebit.api.dto.aboutDto;

import com.codebit.api.dto.footerDto.FootprintDto;
import com.codebit.api.dto.skillDto.SkillDto;
import com.codebit.api.dto.socialLinkDto.SocialLinkDto;
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