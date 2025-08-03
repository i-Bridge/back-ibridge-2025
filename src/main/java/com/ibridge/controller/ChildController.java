package com.ibridge.controller;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.repository.ChildRepository;
import com.ibridge.service.ChildService;
import com.ibridge.service.S3Service;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/child")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService childService;
    private final S3Service s3Service;

    @GetMapping("/{childId}/home")
    public ApiResponse<ChildResponseDTO.getQuestionDTO> home(@PathVariable Long childId) {
        ChildResponseDTO.getQuestionDTO data = childService.getHome(childId);
        return ApiResponse.onSuccess(data);
    }

    @GetMapping("/{childId}/predesigned")
    public ApiResponse<ChildResponseDTO.getPredesignedQuestionDTO> getPredesignedQuestion(@PathVariable Long childId) {
        ChildResponseDTO.getPredesignedQuestionDTO data = childService.getPredesignedQuestion(childId);
        return ApiResponse.onSuccess(data);
    }

    @GetMapping("/{childId}/new")
    public ApiResponse<ChildResponseDTO.getNewQuestionDTO> getNewQuestion(@PathVariable Long childId) {
        ChildResponseDTO.getNewQuestionDTO data = childService.getNewSubject(childId);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/{childId}/answer")
    public ApiResponse<ChildResponseDTO.getAI> getQuestion(@PathVariable Long childId, @RequestBody ChildRequestDTO.AnswerDTO request) {
        ChildResponseDTO.getAI data = childService.getNextQuestion(request);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/{childId}/getURL")
    public ApiResponse<ChildResponseDTO.getPresignedURLDTO> getVideoPresignedURL(@PathVariable Long childId, @RequestBody ChildRequestDTO.GetPresignedURLDTO request) {
        ChildResponseDTO.getPresignedURLDTO data = childService.getPresignedURL(childId, request);
        return ApiResponse.onSuccess(data);
    }

    @PostMapping("/{childId}/uploaded")
    public ApiResponse s3Uploaded(@PathVariable Long childId, @RequestBody ChildRequestDTO.UploadedDTO request) {
        childService.uploaded(request);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/{childId}/finished")
    public ApiResponse finished(@PathVariable Long childId, @RequestBody ChildRequestDTO.FinishedDTO request) {
        childService.answerFinished(childId, request);
        return ApiResponse.onSuccess(null);
    }
}
