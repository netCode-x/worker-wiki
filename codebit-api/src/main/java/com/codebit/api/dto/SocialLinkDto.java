package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocialLinkDto {
    private Long id;
    private String platform;
    private String displayName;
    private String url;
    private String iconClass;
    private String color;
    private Integer sortOrder;
    private Boolean enabled;
}