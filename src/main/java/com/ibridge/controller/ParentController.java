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
    public ApiResponse<ParentResponseDTO.GetMyPageDTO> getMyPage() {
        Long parentId = 0L;

        ParentResponseDTO.GetMyPageDTO parentResponseDTO = parentService.getMyPage(parentId);
        return ApiResponse.onSuccess(parentResponseDTO);
    }

    @GetMapping("/mypage/edit")
    public ApiResponse<ParentResponseDTO.GetFamilyInfoDTO> getFamilyInfo() {
        Long parentId = 0L;

        ParentResponseDTO.GetFamilyInfoDTO data = parentService.getFamilyPage(parentId);
        return ApiResponse.onSuccess(data);
    }

    @PatchMapping("/mypage/edit/familyName")
    public ApiResponse editFamilyName(@RequestBody ParentRequestDTO.editFamilyNameDTO request) {
        Long parentId = 0L;

        parentService.editFamilyName(parentId, request);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/mypage/edit/add")
    public ApiResponse<ParentResponseDTO.ChildIdDTO> addChild(@RequestBody ParentRequestDTO.AddChildDTO addChildDTO) throws ParseException {
        Long parentId = 0L;

        ParentResponseDTO.ChildIdDTO data = parentService.addChild(parentId, addChildDTO);
        return ApiResponse.onSuccess(data);
    }

    @PatchMapping("/mypage/edit/{childId}")
    public ApiResponse<ParentResponseDTO.ChildIdDTO> patchChild(@RequestBody ParentRequestDTO.EditChildDTO request) throws ParseException {
        ParentResponseDTO.ChildIdDTO data = parentService.patchChild(request);
        return ApiResponse.onSuccess(data);
    }

    @DeleteMapping("/mypage/edit/delete")
    public ApiResponse deleteChild(@RequestBody ParentRequestDTO.DeleteChildDTO request) {
        parentService.deleteChild(request);
        return ApiResponse.onSuccess(null);
    }

    //알림
    @GetMapping("/notice")
    public ApiResponse<ParentResponseDTO.NoticeCheckDTO> getNotice() {
        Long parentId = 0L;

        ParentResponseDTO.NoticeCheckDTO data = parentService.getNotice(parentId);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/notice/accept")
    public ApiResponse addParentintoFamily(@PathVariable Long childId, @RequestBody ParentRequestDTO.getParentintoFamilyDTO request) {
        Long parentId = 0L;

        parentService.addParentintoFamily(parentId, request);
        return ApiResponse.onSuccess(null);
    }
}