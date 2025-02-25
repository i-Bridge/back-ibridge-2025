package com.ibridge.controller;

import com.ibridge.util.CustomOAuth2User;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.service.StartService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/start")
@RequiredArgsConstructor
public class StartController {
    private final StartService startService;

    @GetMapping("/signin")
    public ApiResponse<StartResponseDTO> signIn(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ApiResponse.onFailure("401", "인증되지 않은 사용자입니다.");
        }
        StartResponseDTO response = startService.signIn(oAuth2User);
        return ApiResponse.onSuccess(response);
    }
}
