package com.ibridge.controller;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.service.ChildService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/child")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService childService;

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
}
