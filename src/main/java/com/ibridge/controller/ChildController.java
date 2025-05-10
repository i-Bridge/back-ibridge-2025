package com.ibridge.controller;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
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

    @PostMapping("/{childId}/answer")
    public ApiResponse<ChildResponseDTO.getQuestionDTO> getQuestion(@PathVariable Long childId, @RequestBody ChildRequestDTO.AnswerDTO request) {
        ChildResponseDTO.getQuestionDTO data = childService.getNextQuestion(childId, request);
        return ApiResponse.onSuccess(data);
    }

    //s3 저장 경로 양식 : {childId}/{yyyymmdd_hhmmss}.webm / {childId}/{yyyymmdd_hhmmss}.jpeg
    @GetMapping("/{childId}/getURL/{type}")
    public ApiResponse<ChildResponseDTO.getPresignedURLDTO> getVideoPresignedURL(@PathVariable Long childId, @PathVariable String type) {
        LocalDateTime sended = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedSended = sended.format(formatter);
        String contentType = "";
        String objectKey = childId + "/" + formattedSended;

        if(type.equals("video")) {
            contentType = "video/wmeb";
            objectKey = formattedSended + ".wmeb";
        }
        else if(type.equals("image")) {
            contentType = "image/jpeg";
            objectKey = formattedSended + ".jpeg";
        }

        ChildResponseDTO.getPresignedURLDTO data = ChildResponseDTO.getPresignedURLDTO.builder()
                .url(s3Service.generatePresignedUrl(objectKey, contentType, 600)).build();
        return ApiResponse.onSuccess(data);
    }

}
