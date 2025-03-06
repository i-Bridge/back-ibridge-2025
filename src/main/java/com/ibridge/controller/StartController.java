package com.ibridge.controller;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSigninRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.service.LoginService;
import com.ibridge.service.ParentService;
import com.ibridge.service.StartService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/start")
@RequiredArgsConstructor
public class StartController {
    private final StartService startService;
    private final LoginService loginService;

    @GetMapping("/signin")
    public ApiResponse<StartResponseDTO> signIn(@RequestBody StartSigninRequestDTO startSigninRequestDTO) {
        if (startSigninRequestDTO == null) {
            return ApiResponse.onFailure("401", "인증되지 않은 사용자입니다.");
        }
        StartResponseDTO response = startService.signIn(startSigninRequestDTO);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/exist")
    public ApiResponse<StartResponseDTO> checkFamilyExistence(@RequestBody StartRequestDTO request, @RequestHeader("X-User-Email") String email){
        StartResponseDTO response = startService.checkFamilyExistence(request, loginService.getParentFromHeader(email));
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
