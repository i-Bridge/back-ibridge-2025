package com.ibridge.controller;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.entity.Question;
import com.ibridge.repository.AnalysisRepository;
import com.ibridge.repository.QuestionRepository;
import com.ibridge.service.AnalysisService;
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
    private final AnalysisService analysisService;

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

    @GetMapping("/{childId}/home")
    public ApiResponse<ParentHomeResponseDTO> getParentHome(
            @PathVariable Long childId,
            @RequestParam(required = false) String date) {

        ParentHomeResponseDTO response = parentService.getParentHome(childId, date);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{childId}/{questionId}")
    public ApiResponse<QuestionAnalysisDTO> getAnalysis(
            @PathVariable("childId") Long childId,
            @PathVariable("questionId") Long questionId) {
        QuestionAnalysisDTO data = analysisService.getQuestionAnalysis(childId, questionId);
        return ApiResponse.onSuccess(data);
    }

    @GetMapping("/{childId}/notice")
    public ApiResponse<ParentResponseDTO.NoticeCheckDTO> noticeCheck(@PathVariable Long childId) {
        Long parentId = 0L;

        ParentResponseDTO.NoticeCheckDTO data = parentService.noticeCheck(parentId);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/{childId}/add")
    public ApiResponse addFamily(@PathVariable Long childId, @RequestBody ParentRequestDTO.getParentintoFamilyDTO request) {
        Long parentId = 0L;

        parentService.getParentintoFamily(parentId, request);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/{childId}/add-temp")
    public ApiResponse<QuestionResponseDTO> addTempQuestion(
            @PathVariable("childId") Long childId,
            @RequestBody QuestionRequestDTO request) {
        QuestionResponseDTO data = questionService.addTempQuestion(childId, request);
        return ApiResponse.onSuccess(data);
    }

    @GetMapping("/{parentId}/questions/edit/{questionId}")
    public ApiResponse<QuestionResponseDTO> updateQuestion(
            @PathVariable("parentId") Long parentId,
            @PathVariable("questionId") Long questionId) {
        QuestionResponseDTO updatedQuestion = questionService.getQuestionForEdit(parentId, questionId);
        return ApiResponse.onSuccess(updatedQuestion);
    }

    @PutMapping("/{childId}/questions/edit/{questionId}")
    public ApiResponse<?> updateQuestion(
            @PathVariable Long childId,
            @PathVariable Long questionId,
            @RequestBody QuestionUpdateRequestDTO requestDTO) {

        questionService.updateQuestion(childId, questionId, requestDTO);
        return ApiResponse.onSuccess(null);
    }
}
