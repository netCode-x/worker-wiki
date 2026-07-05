package com.codebit.api.dto.noteDto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class NoteQueryRequest {

    private String keyword;      // 搜索关键词

    private Boolean isPrivate;   // 筛选私密/公开

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页大小最小为1")
    private Integer pageSize = 20;
}