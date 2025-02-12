package com.ibridge.controller;

import com.ibridge.domain.dto.response.UserSelectionResponseDTO;
import com.ibridge.service.StartService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/start")
@RequiredArgsConstructor
public class StartController {
    private final StartService startService;

    @GetMapping("/{accountId}/login")
    public ApiResponse<UserSelectionResponseDTO> getUserSelection(@PathVariable("accountId") Long accountId) {
        UserSelectionResponseDTO response = startService.getUserSelection(accountId);
        return ApiResponse.onSuccess(response);
    }
}
