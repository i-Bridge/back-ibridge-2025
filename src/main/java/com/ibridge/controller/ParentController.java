package com.ibridge.controller;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.entity.Question;
import com.ibridge.repository.QuestionRepository;
import com.ibridge.service.ParentService;
import com.ibridge.service.QuestionBoardService;
import com.ibridge.service.QuestionService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/parent")
@RequiredArgsConstructor
public class ParentController {
    private final ParentService parentService;
    private final QuestionBoardService questionBoardService;
    private final QuestionService questionService;

    @GetMapping("/mypage")
    public ApiResponse<ParentResponseDTO.getMyPageDTO> getMyPage() {
        Long parentId = 0L;

        ParentResponseDTO.getMyPageDTO parentResponseDTO = parentService.getMyPage(parentId);
        return ApiResponse.onSuccess(parentResponseDTO);
    }

    @PatchMapping("/mypage/edit")
    public ApiResponse<ParentResponseDTO.EditInfo> EditInfo(@RequestBody ParentRequestDTO.EditInfo editInfo) throws ParseException {
        Long parentId = 0L;

        ParentResponseDTO.EditInfo editInfoDTO = parentService.editInfo(parentId, editInfo);
        return ApiResponse.onSuccess(editInfoDTO);
    }

    @GetMapping("/{parentId}/home")
    public ApiResponse<ParentResponseDTO.ParentHome> getParentHome(@PathVariable("parentId") Long parentId) {
        ParentResponseDTO.ParentHome data = parentService.getParentHomeData(parentId);
        return ApiResponse.onSuccess(data);
    }

    @DeleteMapping("/mypage/delete")
    public ApiResponse<ParentResponseDTO.DeleteDTO> deleteParentHome() {
        Long parentId = 0L;

        parentService.deleteAccount(parentId);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/mypage/child/add")
    public ApiResponse<ParentResponseDTO.AddChildDTO> addChild(@RequestBody ParentRequestDTO.AddChildDTO addChildDTO) throws ParseException {
        Long parentId = 0L;

        ParentResponseDTO.AddChildDTO data = parentService.addChild(parentId, addChildDTO);
        return ApiResponse.onSuccess(data);
    }

    @GetMapping("/{parentId}/{questionId}")
    public ApiResponse<QuestionAnalysisDTO> getQuestionAnalysis(@PathVariable("parentId") Long parentId, @PathVariable("questionId") Long questionId) {
        QuestionAnalysisDTO data = parentService.getQuestionAnalysis(parentId, questionId);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/{parentId}/add-temp")
    public ApiResponse<QuestionResponseDTO> addTempQuestion(@PathVariable("parentId") Long parentId, @RequestBody QuestionRequestDTO request){
        QuestionResponseDTO data = parentService.addTempQuestion(parentId, request);
        return ApiResponse.onSuccess(data);
    }

    @GetMapping("/{parentId}/questions/board")
    public ApiResponse<QuestionBoardResponseDTO> getQuestionBoard(
            @PathVariable("parentId") Long parentId,
            @RequestParam(defaultValue = "1") int page) {
        QuestionBoardResponseDTO data = questionBoardService.getQuestionBoard(parentId, page);
        return ApiResponse.onSuccess(data);
    }

    @PutMapping("/{parentId}/questions/{questionId}")
    public ApiResponse<Void> updateQuestion(
            @PathVariable("parentId") Long parentId,
            @PathVariable("questionId") Long questionId,
            @RequestBody QuestionRequestDTO.QuestionUpdateRequestDTO request) {
        questionService.updateQuestion(parentId, questionId, request);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/{parentId}/questions")
    public ApiResponse<QuestionListResponseDTO> createQuestion(
            @PathVariable("parentId") Long parentId,
            @RequestBody QuestionRequestDTO.QuestionUpdateRequestDTO request) {
        QuestionListResponseDTO data = questionService.createQuestion(parentId, request);
        return ApiResponse.onSuccess(data);
    }

    @DeleteMapping("/{parentId}/questions/delete/{questionId}")
    public ApiResponse<QuestionResponseDTO.DeletedQuestionResponse> deleteQuestion(
            @PathVariable("parentId") Long parentId,
            @PathVariable("questionId") Long questionId) {
        QuestionResponseDTO.DeletedQuestionResponse data = questionService.deleteQuestion(parentId, questionId);
        return ApiResponse.onSuccess(data);
    }

    @PatchMapping("/mypage/child/edit")
    public ApiResponse<ParentResponseDTO.PatchChildDTO> patchChild(@RequestBody ParentRequestDTO.EditChildDTO request) throws ParseException {
        Long parentId = 0L;

        ParentResponseDTO.PatchChildDTO data = parentService.patchChild(parentId, request);
        return ApiResponse.onSuccess(data);
    }

    @DeleteMapping("/mypage/child/delete")
    public ApiResponse deleteChild(@RequestBody ParentRequestDTO.DeleteChildDTO request) {
        parentService.deleteChild(request);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{childId}/notice")
    public ApiResponse<ParentResponseDTO.NoticeCheckDTO> noticeCheck() {
        Long parentId = 0L;

        ParentResponseDTO.NoticeCheckDTO data = parentService.noticeCheck(parentId);
        return ApiResponse.onSuccess(data);
    }
}
