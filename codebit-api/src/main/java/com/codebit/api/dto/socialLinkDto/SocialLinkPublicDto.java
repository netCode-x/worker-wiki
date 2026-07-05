package com.codebit.api.dto.socialLinkDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocialLinkPublicDto {
    private String platform;
    private String displayName;
    private String url;
    private String iconClass;
    private String color;
}