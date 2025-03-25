package com.ibridge.controller;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.service.AnalysisService;
import com.ibridge.service.ParentService;
import com.ibridge.service.QuestionService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/parent")
@RequiredArgsConstructor
public class ParentController {
    private final ParentService parentService;
    private final QuestionService questionService;
    private final AnalysisService analysisService;

//지웅
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

    @PostMapping("/{childId}/questions/add-regular")
    public ApiResponse<?> addRegularQuestion(
            @PathVariable Long childId,
            @RequestBody(required = false) QuestionRequestDTO requestDTO) {
        questionService.addRegularQuestion(childId, requestDTO);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{childId}/questions/regular")
    public ApiResponse<List<QuestionResponseDTO>> getRegularQuestions(
            @PathVariable Long childId) {

        List<QuestionResponseDTO> questions = questionService.getRegularQuestions(childId);
        return ApiResponse.onSuccess(questions);
    }
    @DeleteMapping("/{childId}/questions/delete/{questionId}")
    public ApiResponse<Void> deleteQuestion(
            @PathVariable Long childId,
            @PathVariable Long questionId) {

        questionService.deleteQuestion(childId, questionId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{childId}/questions/board")
    public ApiResponse<QuestionBoardResponseDTO> getQuestionBoard(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        QuestionBoardResponseDTO response = questionService.getQuestionBoard(childId, page, size);
        return ApiResponse.onSuccess(response);
    }










//현호
    //마이페이지
    @GetMapping("/mypage")
    public ApiResponse<ParentResponseDTO.getMyPageDTO> getMyPage() {
        Long parentId = 0L;

        ParentResponseDTO.getMyPageDTO parentResponseDTO = parentService.getMyPage(parentId);
        return ApiResponse.onSuccess(parentResponseDTO);
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

    //알림
    @DeleteMapping("/{childId}/notice/delete")
    public ApiResponse deleteNotice(@PathVariable("childId") Long childId, ParentRequestDTO.DeleteNoticeDTO request) {
        Long parentId = 0L;
        parentService.deleteNotice(parentId, request);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/{childId}/notice/deleteAll")
    public ApiResponse deleteNoticeAll(@PathVariable("childId") Long childId) {
        Long parentId = 0L;
        parentService.deleteNoticeAll(parentId);
        return ApiResponse.onSuccess(null);
    }
}