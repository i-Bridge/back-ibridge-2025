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

//지웅
    @GetMapping("/{childId}/home")
    public ApiResponse<ParentHomeResponseDTO> getParentHome(
            @PathVariable Long childId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ParentHomeResponseDTO response = parentService.getParentHome(childId, date);
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