package com.codebit.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteUpdateRequest {

    @NotNull(message = "随记ID不能为空")
    private Long id;

    private String content;

    private Boolean isPrivate;
}