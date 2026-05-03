package com.codebit.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteCreateRequest {

    @NotBlank(message = "内容不能为空")
    private String content;

    private Boolean isPrivate = false;  // 默认公开
}