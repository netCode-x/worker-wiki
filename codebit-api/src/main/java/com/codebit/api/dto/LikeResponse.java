package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeResponse {
    private Long likeCount;
    private String message;
    private Boolean success;
}