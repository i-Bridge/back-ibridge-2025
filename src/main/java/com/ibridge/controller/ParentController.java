package com.ibridge.controller;

import com.ibridge.domain.dto.request.EditQuestionRequestDTO;
import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.entity.Parent;
import com.ibridge.service.AnalysisService;
import com.ibridge.service.LoginService;
import com.ibridge.service.ParentService;
import com.ibridge.service.QuestionService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDate;

@RestController
@RequestMapping("/parent")
@RequiredArgsConstructor
public class ParentController {
    private final ParentService parentService;
    private final QuestionService questionService;
    private final AnalysisService analysisService;
    private final LoginService loginService;

    //지웅
    @GetMapping("/{childId}/home")
    public ApiResponse<ParentHomeResponseDTO> getParentHome(
            @PathVariable Long childId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader("X-User-Email") String email) {
        ParentHomeResponseDTO response = parentService.getParentHome(childId, date, email);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{childId}/{subjectId}")
    public ApiResponse<QuestionAnalysisDTO> getAnalysis(
            @PathVariable Long childId,
            @PathVariable Long subjectId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        QuestionAnalysisDTO response = questionService.getQuestionAnalysis(childId, subjectId, date);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{childId}/{subjectId}/{questionId}")
    public ApiResponse<QuestionDetailResponseDTO> getQuestionDetail(
            @PathVariable Long childId,
            @PathVariable Long subjectId,
            @PathVariable Long questionId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        QuestionDetailResponseDTO response = questionService.getQuestionDetail(childId, subjectId, questionId, date);
        return ApiResponse.onSuccess(response);
    }

    @PutMapping("/{childId}/questions/edit")
    public ApiResponse<Void> editQuestion(
            @PathVariable Long childId,
            @RequestBody EditQuestionRequestDTO request) {

        questionService.editQuestion(childId, request);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{childId}/questions/reroll")
    public ApiResponse<SubjectResponseDTO> rerollQuestion(@PathVariable Long childId) {
        SubjectResponseDTO response = questionService.rerollQuestion(childId);
        return ApiResponse.onSuccess(response);
    }












//현호
    //마이페이지
    @GetMapping("/mypage")
    public ApiResponse<ParentResponseDTO.GetMyPageDTO> getMyPage(@RequestHeader("X-User-Email") String email) {
        Long parentId = loginService.getParentFromHeader(email).getId();

        ParentResponseDTO.GetMyPageDTO parentResponseDTO = parentService.getMyPage(parentId);
        return ApiResponse.onSuccess(parentResponseDTO);
    }

    @GetMapping("/mypage/edit")
    public ApiResponse<ParentResponseDTO.GetFamilyInfoDTO> getFamilyInfo(@RequestHeader("X-User-Email") String email) {
        Long parentId = loginService.getParentFromHeader(email).getId();

        ParentResponseDTO.GetFamilyInfoDTO data = parentService.getFamilyPage(parentId);
        return ApiResponse.onSuccess(data);
    }

    @PatchMapping("/mypage/edit/familyName")
    public ApiResponse editFamilyName(@RequestHeader("X-User-Email") String email, @RequestBody ParentRequestDTO.editFamilyNameDTO request) {
        Long parentId = loginService.getParentFromHeader(email).getId();

        parentService.editFamilyName(parentId, request);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/mypage/edit/add")
    public ApiResponse<ParentResponseDTO.ChildIdDTO> addChild(@RequestHeader("X-User-Email") String email, @RequestBody ParentRequestDTO.AddChildDTO addChildDTO) throws ParseException {
        Long parentId = loginService.getParentFromHeader(email).getId();

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
    public ApiResponse<ParentResponseDTO.NoticeCheckDTO> getNotice(@RequestHeader("X-User-Email") String email) {
        Long parentId = loginService.getParentFromHeader(email).getId();

        ParentResponseDTO.NoticeCheckDTO data = parentService.getNotice(parentId);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/notice/accept")
    public ApiResponse addParentintoFamily(@RequestHeader("X-User-Email") String email, @RequestBody ParentRequestDTO.getParentintoFamilyDTO request) {
        Long parentId = loginService.getParentFromHeader(email).getId();

        parentService.addParentintoFamily(parentId, request);
        return ApiResponse.onSuccess(null);
    }
}