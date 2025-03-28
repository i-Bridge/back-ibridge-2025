package com.ibridge.controller;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSigninRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.service.LoginService;
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

    @PostMapping("/signin")
    public ApiResponse<StartResponseDTO> signIn(@RequestHeader("X-User-Email") String email, @RequestHeader("X-User-Name") String name) {
        if (email.isEmpty()) {
            return ApiResponse.onFailure("401", "인증되지 않은 사용자입니다.");
        }
        StartResponseDTO response = startService.signIn(email, name);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/exist")
    public ApiResponse<?> checkFamilyExistence(@RequestBody StartRequestDTO request, @RequestHeader("X-User-Email") String email) {
        startService.checkFamilyExistence(request, loginService.getParentFromHeader(email));
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/signup/dup")
    public ApiResponse<Boolean> checkDuplicateFamilyName(@RequestBody StartRequestDTO request, @RequestHeader("X-User-Email") String email){
        Boolean response = startService.checkFamilyDuplicate(request, loginService.getParentFromHeader(email));
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/new")
    public ApiResponse<?> registerNewFamily(@RequestBody StartSignupNewRequestDTO request, @RequestHeader("X-User-Email") String email) {
        startService.registerNewChildren(request, email);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/login")
    public ApiResponse<StartUserSelectionResponseDTO> getUserSelection(@RequestHeader("X-User-Email") String email) {
        StartUserSelectionResponseDTO response = startService.getUserSelection(loginService.getParentFromHeader(email));
        return ApiResponse.onSuccess(response);
    }
}
