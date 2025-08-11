package com.ibridge.controller;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
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
        try {
            ChildResponseDTO.getQuestionDTO data = childService.getHome(childId);
            return ApiResponse.onSuccess(data);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/predesigned")
    public ApiResponse<ChildResponseDTO.getPredesignedQuestionDTO> getPredesignedQuestion(@PathVariable Long childId) {
        try {
            ChildResponseDTO.getPredesignedQuestionDTO data = childService.getPredesignedQuestion(childId);
            return ApiResponse.onSuccess(data);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @GetMapping("/{childId}/new")
    public ApiResponse<ChildResponseDTO.getNewQuestionDTO> getNewQuestion(@PathVariable Long childId) {
        try {
            ChildResponseDTO.getNewQuestionDTO data = childService.getNewSubject(childId);
            return ApiResponse.onSuccess(data);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/{childId}/answer")
    public ApiResponse<ChildResponseDTO.getAI> getQuestion(@PathVariable Long childId, @RequestBody ChildRequestDTO.AnswerDTO request) {
        try {
            ChildResponseDTO.getAI data = childService.getNextQuestion(request);
            return ApiResponse.onSuccess(data);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/{childId}/getURL")
    public ApiResponse<ChildResponseDTO.getPresignedURLDTO> getVideoPresignedURL(@PathVariable Long childId, @RequestBody ChildRequestDTO.GetPresignedURLDTO request) {
        try {
            ChildResponseDTO.getPresignedURLDTO data = childService.getPresignedURL(childId, request);
            return ApiResponse.onSuccess(data);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/{childId}/uploaded")
    public ApiResponse s3Uploaded(@PathVariable Long childId, @RequestBody ChildRequestDTO.UploadedDTO request) {
        try {
            childService.uploaded(request);
            return ApiResponse.onSuccess(null);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }

    @PostMapping("/{childId}/finished")
    public ApiResponse finished(@PathVariable Long childId, @RequestBody ChildRequestDTO.FinishedDTO request) {
        try {
            childService.answerFinished(childId, request);
            return ApiResponse.onSuccess(null);
        }
        catch (Exception e) {
            System.out.println("failure return: " + e.getMessage());
            return ApiResponse.onFailure("404", e.getMessage());
        }
    }
}
