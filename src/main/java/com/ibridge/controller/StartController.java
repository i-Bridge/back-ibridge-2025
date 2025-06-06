package com.ibridge.controller;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSigninRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.FamilyExistDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.service.LoginService;
import com.ibridge.service.StartService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/start")
@RequiredArgsConstructor
public class StartController {
    private final StartService startService;
    private final LoginService loginService;

    @PostMapping("/signin")
    public ApiResponse<StartResponseDTO> signIn(@RequestBody StartSigninRequestDTO request) {
        StartResponseDTO response = startService.signIn(request.getEmail(), request.getName());
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/exist")
    public ApiResponse<FamilyExistDTO> checkFamilyExistence(HttpServletRequest r, @RequestBody StartRequestDTO request) {
        String email = (String) r.getAttribute("email");
        FamilyExistDTO response = startService.checkFamilyExistence(request, loginService.getParentFromHeader(email));
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/dup")
    public ApiResponse<FamilyExistDTO> checkDuplicateFamilyName(@RequestBody StartRequestDTO request){
        FamilyExistDTO response = startService.checkFamilyDuplicate(request);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/signup/new")
    public ApiResponse<?> registerNewFamily(HttpServletRequest r, @RequestBody StartSignupNewRequestDTO request) {
        String email = (String) r.getAttribute("email");

        startService.registerNewChildren(request, email);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/login")
    public ApiResponse<StartUserSelectionResponseDTO> getUserSelection(HttpServletRequest r) {
        String email = (String) r.getAttribute("email");
        StartUserSelectionResponseDTO response = startService.getUserSelection(loginService.getParentFromHeader(email));
        return ApiResponse.onSuccess(response);
    }
}
