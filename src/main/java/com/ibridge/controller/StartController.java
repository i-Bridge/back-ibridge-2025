package com.ibridge.controller;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSigninRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.util.CustomOAuth2User;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.service.StartService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/start")
@RequiredArgsConstructor
public class StartController {
    private final StartService startService;

    @PostMapping("/signin")
    public ApiResponse<StartResponseDTO> signIn(@RequestBody StartSigninRequestDTO startSigninRequestDTO) {
        if (startSigninRequestDTO == null) {
            return ApiResponse.onFailure("401", "인증되지 않은 사용자입니다.");
        }
        StartResponseDTO response = startService.signIn(startSigninRequestDTO);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/exist")
    public ApiResponse<StartResponseDTO> checkFamilyExistence(@RequestBody StartRequestDTO request){
        StartResponseDTO response = startService.checkFamilyExistence(request);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/new")
    public ApiResponse<StartResponseDTO> registerNewFamily(@RequestBody StartSignupNewRequestDTO request) {
        StartResponseDTO response = startService.registerNewFamily(request);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/login")
    public ApiResponse<StartUserSelectionResponseDTO> getUserSelection(@RequestParam Long parentId) {
        StartUserSelectionResponseDTO response = startService.getUserSelection(parentId);
        return ApiResponse.onSuccess(response);
    }
}
