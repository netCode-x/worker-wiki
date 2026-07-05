package com.codebit.api.controller;

import com.codebit.api.dto.*;
import com.codebit.api.dto.noteDto.NoteCreateRequest;
import com.codebit.api.dto.noteDto.NoteQueryRequest;
import com.codebit.api.dto.noteDto.NoteResponse;
import com.codebit.api.dto.noteDto.NoteUpdateRequest;
import com.codebit.api.service.NoteService;
import com.codebit.api.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "随记接口")
public class NoteController {

    private final NoteService noteService;

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 创建随记
     */

    @RequestMapping(path = "/create",method = RequestMethod.POST)
    @Operation(summary = "创建随记")
    public Result<NoteResponse> createNote(@Valid @RequestBody NoteCreateRequest request) {
        Long authorId = getCurrentUserId();
        log.info("创建随记: authorId={}, content长度={}", authorId, request.getContent().length());
        NoteResponse response = noteService.createNote(request, authorId);
        return Result.success(response);
    }

    /**
     * 更新随记
     */

    @RequestMapping(path = "/update",method = RequestMethod.PUT)
    @Operation(summary = "更新随记")
    public Result<NoteResponse> updateNote(@Valid @RequestBody NoteUpdateRequest request) {
        Long authorId = getCurrentUserId();
        log.info("更新随记: id={}, authorId={}", request.getId(), authorId);
        NoteResponse response = noteService.updateNote(request, authorId);
        return Result.success(response);
    }

    /**
     * 删除随记
     */

    @RequestMapping(path = "/{id}",method = RequestMethod.DELETE)
    @Operation(summary = "删除随记")
    public Result<Void> deleteNote(@PathVariable Long id) {
        Long authorId = getCurrentUserId();
        log.info("删除随记: id={}, authorId={}", id, authorId);
        noteService.deleteNote(id, authorId);
        return Result.success(null);
    }

    /**
     * 获取随记详情
     */

    @RequestMapping(path = "/detail/{id}",method = RequestMethod.GET)
    @Operation(summary = "获取随记详情")
    public Result<NoteResponse> getNote(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        NoteResponse response = noteService.getNoteById(id, currentUserId);
        return Result.success(response);
    }

    /**
     * 获取我的随记列表
     */

    @RequestMapping(path = "/mynotes",method = RequestMethod.GET)
    @Operation(summary = "获取我的随记列表")
    public Result<PageResult<NoteResponse>> getMyNotes(@Valid NoteQueryRequest request) {
        Long authorId = getCurrentUserId();
        log.info("查询我的随记: authorId={}, pageNum={}, pageSize={}", authorId, request.getPageNum(), request.getPageSize());
        PageResult<NoteResponse> result = noteService.getMyNotes(request, authorId);
        return Result.success(result);
    }

    /**
     * 获取公开随记列表（所有人可见）
     */

    @RequestMapping(path = "/public", method = RequestMethod.GET)
    @Operation(summary = "获取公开随记列表（所有人可见）")
    public Result<PageResult<NoteResponse>> getPublicNotes(@Valid NoteQueryRequest request) {
        log.info("查询公开随记: pageNum={}, pageSize={}, keyword={}", request.getPageNum(), request.getPageSize(), request.getKeyword());
        PageResult<NoteResponse> result = noteService.getPublicNotes(request);
        return Result.success(result);
    }

    /**
     * 获取所有公开随记（不分页，直接返回列表）
     * GET /api/notes/public/all?keyword=xxx
     */
    @RequestMapping(path = "/public/all",method = RequestMethod.GET)
    @Operation(summary = "获取公开随记列表（所有人可见）")
    public Result<List<NoteResponse>> getAllPublicNotes() {
        List<NoteResponse> notes = noteService.getAllPublicNotes();
        return Result.success(notes);
    }

}