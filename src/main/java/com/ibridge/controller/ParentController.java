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
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
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
    @GetMapping("{childId}/readSubjects")
    public ApiResponse<readSubjectsResponseDTO> readSubjects(HttpServletRequest r, @PathVariable Long childId, @RequestParam(value = "year") Long year, @RequestParam(value = "month") Long month) {
        String email = (String)r.getAttribute("email");
        readSubjectsResponseDTO response = parentService.readSubjects(loginService.getParentFromHeader(email), childId, year, month);
        return ApiResponse.onSuccess(response);
    }
    @GetMapping("/{childId}/home")
    public ApiResponse<ParentHomeResponseDTO> getParentHome(
            @PathVariable Long childId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest r) {
        if (date == null) {
            date = LocalDate.now();
        }
        String email = (String) r.getAttribute("email");
        ParentHomeResponseDTO response = parentService.getParentHome(childId, date, email);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{childId}/{subjectId}")
    public ApiResponse<QuestionAnalysisDTO> getAnalysis(HttpServletRequest r,
            @PathVariable Long childId,
            @PathVariable Long subjectId) {
        String email = (String) r.getAttribute("email");
        QuestionAnalysisDTO response = questionService.getQuestionAnalysis(loginService.getParentFromHeader(email), childId, subjectId);
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

    @PatchMapping("/{childId}/questions/edit")
    public ApiResponse<Void> editQuestion(
            @PathVariable Long childId,
            @RequestBody EditQuestionRequestDTO request,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        questionService.editQuestion(childId, request, date);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{childId}/questions/reroll")
    public ApiResponse<SubjectResponseDTO> rerollQuestion(@PathVariable Long childId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        SubjectResponseDTO response = questionService.rerollQuestion(childId, date);
        return ApiResponse.onSuccess(response);
    }












//현호
    //마이페이지
    @GetMapping("/mypage")
    public ApiResponse<ParentResponseDTO.GetMyPageDTO> getMyPage(HttpServletRequest r) {
        try {
            Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();
            ParentResponseDTO.GetMyPageDTO parentResponseDTO = parentService.getMyPage(parentId);
            return ApiResponse.onSuccess(parentResponseDTO);
        }
        catch(Exception e) {
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/mypage/edit")
    public ApiResponse<ParentResponseDTO.GetFamilyInfoDTO> getFamilyInfo(HttpServletRequest r) {
        Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

        ParentResponseDTO.GetFamilyInfoDTO data = parentService.getFamilyPage(parentId);
        return ApiResponse.onSuccess(data);
    }

    @PatchMapping("/mypage/edit/familyName")
    public ApiResponse editFamilyName(HttpServletRequest r, @RequestBody ParentRequestDTO.editFamilyNameDTO request) {
        Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

        parentService.editFamilyName(parentId, request);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/mypage/edit/add")
    public ApiResponse<ParentResponseDTO.ChildIdDTO> addChild(HttpServletRequest r, @RequestBody ParentRequestDTO.AddChildDTO addChildDTO) throws ParseException {
        Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

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
    public ApiResponse<ParentResponseDTO.NoticeCheckDTO> getNotice(HttpServletRequest r) {
        Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

        ParentResponseDTO.NoticeCheckDTO data = parentService.getNotice(parentId);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/notice/accept")
    public ApiResponse addParentintoFamily(HttpServletRequest r, @RequestBody ParentRequestDTO.getParentintoFamilyDTO request) {
        Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

        parentService.addParentintoFamily(parentId, request);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/notice/decline")
    public ApiResponse declineParentintoFamily(HttpServletRequest r, @RequestBody ParentRequestDTO.getParentintoFamilyDTO request) {
        Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

        parentService.declineParentintoFamily(parentId, request);
        return ApiResponse.onSuccess(null);
    }
}