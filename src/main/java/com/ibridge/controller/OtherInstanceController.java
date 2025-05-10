package com.ibridge.controller;

import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.service.OtherService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/back")
@RequiredArgsConstructor
public class OtherInstanceController {
    private final OtherService otherService;

    @GetMapping("/setSubject")
    public ApiResponse setSubject() {
        otherService.setSubject();
        return ApiResponse.onSuccess(null);
    }
}
