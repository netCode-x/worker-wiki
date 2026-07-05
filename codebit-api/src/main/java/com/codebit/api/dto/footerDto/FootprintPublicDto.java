package com.codebit.api.dto.footerDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FootprintPublicDto {
    private Long id;
    private Integer year;
    private Integer month;
    private String title;
    private String description;
    private String location;
    private String icon;
    private String imageUrl;
    private Boolean isHighlight;
    private String linkUrl;
    private String linkText;
    private String formattedDate;
}