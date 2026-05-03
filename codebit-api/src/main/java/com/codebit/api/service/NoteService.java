package com.codebit.api.service;

import com.codebit.api.dto.*;
import com.codebit.api.entity.Note;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/15 星期三
 * @Description:
 * @VERSON: 17
 */
public interface NoteService {

    NoteResponse createNote(NoteCreateRequest request, Long authorId);

    NoteResponse updateNote(NoteUpdateRequest request, Long authorId);

    void deleteNote(Long id, Long authorId);


    NoteResponse getNoteById(Long id, Long currentUserId);


    PageResult<NoteResponse> getMyNotes(NoteQueryRequest request, Long authorId);

    PageResult<NoteResponse> getPublicNotes(NoteQueryRequest request);

    List<NoteResponse> getAllPublicNotes();
}
