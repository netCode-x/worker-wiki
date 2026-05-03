package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AboutHistoryResponse {
    private Long id;
    private Integer version;
    private String content;
    private String contentHtml;
    private String socialLinksSnapshot;
    private String skillsSnapshot;
    private String footprintsSnapshot;
    private String changedBy;
    private String changeReason;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
}