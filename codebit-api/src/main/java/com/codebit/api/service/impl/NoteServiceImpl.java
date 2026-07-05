package com.codebit.api.service.impl;

import com.codebit.api.converter.BuildResponseConverter;
import com.codebit.api.dto.*;
import com.codebit.api.dto.noteDto.NoteCreateRequest;
import com.codebit.api.dto.noteDto.NoteQueryRequest;
import com.codebit.api.dto.noteDto.NoteResponse;
import com.codebit.api.dto.noteDto.NoteUpdateRequest;
import com.codebit.api.entity.Note;
import com.codebit.api.entity.User;
import com.codebit.api.repository.NoteRepository;
import com.codebit.api.repository.UserRepository;
import com.codebit.api.service.NoteService;
import com.codebit.api.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/15 星期三
 * @Description:
 * @VERSON: 17
 */

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoteServiceImpl implements NoteService {


    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final BuildResponseConverter buildResponse;


    private static final Integer STATUS_NORMAL = 1;
    private static final Integer STATUS_DELETED = 2;




    /***
     * 创建随记
     * @param request
     * @param authorId
     * @return
     */
    @Transactional
    @Override
    public NoteResponse createNote(NoteCreateRequest request, Long authorId) {
        Note note = Note.builder()
                .content(request.getContent())
                .authorId(authorId)
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .status(STATUS_NORMAL)
                .build();


        Note saveNote = noteRepository.save(note);
        log.info("随记创建成功： id={}, authorId={}", saveNote.getId(), saveNote.getAuthorId());
        return buildResponse.buildResponse(saveNote, null);
    }


    /**
     * 随记更新
     * @param request
     * @param authorId
     * @return
     */
    @Transactional
    @Override
    public NoteResponse updateNote (NoteUpdateRequest request, Long authorId){

        Note note = noteRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(404, "随记不存在"));
        if (!note.getAuthorId().equals(authorId)){
            throw  new BusinessException(403,"无权限修改此随记");
        }
        if (request.getContent() !=null){
            note.setContent(request.getContent());
        }
        if (request.getIsPrivate() !=null){
            note.setIsPrivate(request.getIsPrivate());
        }
        Note updateNote = noteRepository.save(note);

        log.info("随记更新成功： id={},authorId={}",updateNote.getId(),authorId);
        return buildResponse.buildResponse(updateNote,null);

    }
    /**
     * 删除随记（软删除）
     */
    @Transactional
    @Override
    public void deleteNote(Long id, Long authorId) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "随记不存在"));

        if (!note.getAuthorId().equals(authorId)) {
            throw new BusinessException(403, "无权限删除此随记");
        }

        note.setStatus(STATUS_DELETED);
        noteRepository.save(note);
        log.info("随记删除成功: id={}, authorId={}", id, authorId);
    }

    /**
     * 获取随记详情
     */
    @Transactional(readOnly = true)
    @Override
    public NoteResponse getNoteById(Long id, Long currentUserId){
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "随记不存在"));

        if (note.getStatus() == STATUS_DELETED) {
            throw new BusinessException(404, "随记不存在");
        }

        // 私密随记只有作者本人能看
        if (note.getIsPrivate() && !note.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(403, "无权限查看此随记");
        }
        User author = userRepository.findById(note.getAuthorId()).orElse(null);
        return buildResponse.buildResponse(note, author);
    }

    /**
     * 获取随记列表（我的随记）
     */
    @Transactional(readOnly = true)
    @Override
    public PageResult<NoteResponse> getMyNotes(NoteQueryRequest request, Long authorId){
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        Pageable pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize(), sort);

        Page<Note> notePage;

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            notePage = noteRepository.searchByAuthorAndKeyword(authorId, STATUS_NORMAL, request.getKeyword(), pageable);
        } else {
            notePage = noteRepository.findByAuthorIdAndStatusOrderByCreateDateDesc(authorId, STATUS_NORMAL, pageable);
        }

        // 批量查询作者信息（其实都是同一个作者，但保持通用）
        List<Long> authorIds = notePage.getContent().stream()
                .map(Note::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<NoteResponse> responseList = notePage.getContent().stream()
                .map(note -> buildResponse.buildResponse(note, userMap.get(note.getAuthorId())))
                .collect(Collectors.toList());
        return new PageResult<>(
                notePage.getTotalElements(),
                request.getPageNum(),
                request.getPageSize(),
                notePage.getTotalPages(),
                responseList
        );
    }
    /**
     * 获取公开随记列表（所有人可见）
     */
    @Transactional(readOnly = true)
    @Override
    public PageResult<NoteResponse> getPublicNotes(NoteQueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        Pageable pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize(), sort);

        Page<Note> notePage;

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            notePage = noteRepository.searchPublicByKeyword(STATUS_NORMAL, request.getKeyword(), pageable);
        } else {
            notePage = noteRepository.findByIsPrivateFalseAndStatusOrderByCreateDateDesc(STATUS_NORMAL, pageable);
        }

        List<Long> authorIds = notePage.getContent().stream()
                .map(Note::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<NoteResponse> responseList = notePage.getContent().stream()
                .map(note -> buildResponse.buildResponse(note, userMap.get(note.getAuthorId())))
                .collect(Collectors.toList());

        return new PageResult<>(
                notePage.getTotalElements(),
                request.getPageNum(),
                request.getPageSize(),
                notePage.getTotalPages(),
                responseList
        );
    }

    /**
     * 获取所有公开随记（不分页）
     * @return 公开随记列表
     */
    public List<NoteResponse> getAllPublicNotes() {
        // 1. 查询所有符合条件的公开随记（按创建时间倒序）
        List<Note> noteList;

        noteList = noteRepository.findByIsPrivateFalseAndStatusOrderByCreateDateDesc(STATUS_NORMAL);

        // 2. 如果列表为空，直接返回空列表
        if (noteList.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 批量查询作者信息（避免 N+1 问题）
        List<Long> authorIds = noteList.stream()
                .map(Note::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 4. 转换为响应 DTO
        return noteList.stream()
                .map(note -> buildResponse.buildResponse(note, userMap.get(note.getAuthorId())))
                .collect(Collectors.toList());
    }




}
