package com.ibridge.controller;

import com.ibridge.domain.dto.request.*;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    @PostMapping("/{childId}/pressNoice")
    public ApiResponse<?> openNotice(@PathVariable Long childId, @RequestParam(value = "subjectId") Long subjectId, @RequestParam(value = "noticeId") Long noticeId){
        try{
            parentService.openNotice(childId, subjectId, noticeId);
            return ApiResponse.onSuccess(null);
        }
        catch (Exception e){
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }
    @GetMapping("{childId}/readSubjects")
    public ApiResponse<readSubjectsResponseDTO> readSubjects(HttpServletRequest r, @PathVariable Long childId, @RequestParam(value = "year") Long year, @RequestParam(value = "month") Long month) {
        try {
            String email = (String)r.getAttribute("email");
            readSubjectsResponseDTO response = parentService.readSubjects(loginService.getParentFromHeader(email), childId, year, month);
            return ApiResponse.onSuccess(response);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }
    @GetMapping("/{childId}/home")
    public ApiResponse<ParentHomeResponseDTO> getParentHome(
            @PathVariable Long childId,
            @PageableDefault(page = 0, size = 7) Pageable pageable,
            HttpServletRequest r) {

        try {
            String email = (String) r.getAttribute("email");
            ParentHomeResponseDTO response = parentService.getParentHome(childId, pageable, email);
            return ApiResponse.onSuccess(response);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/scheduled")
    public ApiResponse<ScheduledDTO> getAnalysis(HttpServletRequest r,
                                                        @PathVariable Long childId) {
        try {
            String email = (String) r.getAttribute("email");
            ScheduledDTO response = questionService.getScheduled(childId);
            return ApiResponse.onSuccess(response);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/banner")
    public ApiResponse<BannerDTO> getBanner(HttpServletRequest r,
                                                 @PathVariable Long childId) {

        try {
            String email = (String) r.getAttribute("email");
            BannerDTO response = parentService.getBanner(childId);
            return ApiResponse.onSuccess(response);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/{subjectId}")
    public ApiResponse<QuestionAnalysisDTO> getAnalysis(HttpServletRequest r,
            @PathVariable Long childId,
            @PathVariable Long subjectId) {

        try {
            String email = (String) r.getAttribute("email");
            QuestionAnalysisDTO response = questionService.getQuestionAnalysis(loginService.getParentFromHeader(email), childId, subjectId);
            return ApiResponse.onSuccess(response);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/{subjectId}/{questionId}")
    public ApiResponse<QuestionDetailResponseDTO> getQuestionDetail(
            @PathVariable Long childId,
            @PathVariable Long subjectId,
            @PathVariable Long questionId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            QuestionDetailResponseDTO response = questionService.getQuestionDetail(childId, subjectId, questionId, date);
            return ApiResponse.onSuccess(response);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PatchMapping("/{childId}/questions/edit")
    public ApiResponse<Void> editQuestion(
            @PathVariable Long childId,
            @RequestBody EditQuestionRequestDTO request,
            @RequestParam("subjectId") Long subjectId) {

        try {
            questionService.editQuestion(childId, request, subjectId);
            return ApiResponse.onSuccess(null);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/questions/reroll")
    public ApiResponse<SubjectResponseDTO> rerollQuestion(@PathVariable Long childId, @RequestParam("subjectId") Long subjectId) {
        try {
            SubjectResponseDTO response = questionService.rerollQuestion(childId, subjectId);
            return ApiResponse.onSuccess(response);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/stat")
    public ApiResponse<AnalysisResponseDTO> getAnalysis(@PathVariable Long childId){
        try{
            AnalysisResponseDTO analysisResponseDTO = parentService.getDefaultAnalysis(childId);
            return ApiResponse.onSuccess(analysisResponseDTO);
        }
        catch(Exception e){
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("{childId}/stat/emotion")
    public ApiResponse<AnalysisResponseDTO> getEmotions(@PathVariable Long childId, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        try{
            AnalysisResponseDTO analysisResponseDTO = parentService.getEmotions(childId, date);
            return ApiResponse.onSuccess(analysisResponseDTO);
        }
        catch(Exception e){
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("{childId}/stat/cumulative")
    public ApiResponse<AnalysisResponseDTO> getCumulatives(@PathVariable Long childId, @RequestParam("periodType") String periodType){
        try{
            AnalysisResponseDTO analysisResponseDTO = parentService.getCumulatives(childId, periodType);
            return ApiResponse.onSuccess(analysisResponseDTO);
        }
        catch(Exception e){
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("{childId}/stat/{keyword}")
    public ApiResponse<CategorySubjectDTO> getSubjects(@PathVariable Long childId, @PathVariable String keyword){
        try{
            CategorySubjectDTO categorySubjectDTO  = parentService.getSubjects(childId, keyword);
            return ApiResponse.onSuccess(categorySubjectDTO);
        }
        catch(Exception e){
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
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
        try {
            Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

            ParentResponseDTO.GetFamilyInfoDTO data = parentService.getFamilyPage(parentId);
            return ApiResponse.onSuccess(data);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PatchMapping("/mypage/edit/familyName")
    public ApiResponse editFamilyName(HttpServletRequest r, @RequestBody ParentRequestDTO.editFamilyNameDTO request) {
        try {
            Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

            parentService.editFamilyName(parentId, request);
            return ApiResponse.onSuccess(null);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/mypage/edit/add")
    public ApiResponse<ParentResponseDTO.ChildIdDTO> addChild(HttpServletRequest r, @RequestBody ParentRequestDTO.AddChildDTO addChildDTO) throws ParseException {
        try {
            Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

            ParentResponseDTO.ChildIdDTO data = parentService.addChild(parentId, addChildDTO);
            return ApiResponse.onSuccess(data);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PatchMapping("/mypage/edit/{childId}")
    public ApiResponse<ParentResponseDTO.ChildIdDTO> patchChild(@RequestBody ParentRequestDTO.EditChildDTO request) throws ParseException {
        try {
            ParentResponseDTO.ChildIdDTO data = parentService.patchChild(request);
            return ApiResponse.onSuccess(data);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @DeleteMapping("/mypage/edit/delete")
    public ApiResponse deleteChild(@RequestBody ParentRequestDTO.DeleteChildDTO request) {
        try {
            parentService.deleteChild(request);
            return ApiResponse.onSuccess(null);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }

    }

    //알림
    @GetMapping("/notice")
    public ApiResponse<ParentResponseDTO.NoticeCheckDTO> getNotice(HttpServletRequest r) {
        try {
            Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

            ParentResponseDTO.NoticeCheckDTO data = parentService.getNotice(parentId);
            return ApiResponse.onSuccess(data);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/notice/accept")
    public ApiResponse addParentintoFamily(HttpServletRequest r, @RequestBody ParentRequestDTO.getParentintoFamilyDTO request) {
        try {
            Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

            parentService.addParentintoFamily(parentId, request);
            return ApiResponse.onSuccess(null);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/notice/decline")
    public ApiResponse declineParentintoFamily(HttpServletRequest r, @RequestBody ParentRequestDTO.getParentintoFamilyDTO request) {
        try {
            Long parentId = loginService.getParentFromHeader((String) r.getAttribute("email")).getId();

            parentService.declineParentintoFamily(parentId, request);
            return ApiResponse.onSuccess(null);
        }
        catch(Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/notice/readAll")
    public ApiResponse readAll(HttpServletRequest r) {
        try {
            Parent parent = loginService.getParentFromHeader((String) r.getAttribute("email"));

            parentService.readAll(parent);
            return ApiResponse.onSuccess(null);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }
}