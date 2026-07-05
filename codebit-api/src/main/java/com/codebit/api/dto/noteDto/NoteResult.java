package com.codebit.api.dto.noteDto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResult<T> {


    private Integer id;

    private LocalDateTime dataTime;

    private String title;

    private String content;

    private String summary;

}
