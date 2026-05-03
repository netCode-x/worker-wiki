package com.codebit.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NoteResponse {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private Boolean isPrivate;
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}