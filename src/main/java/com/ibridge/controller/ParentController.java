package com.ibridge.controller;

import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.service.ParentService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parent")
@RequiredArgsConstructor
public class ParentController {
    private final ParentService parentService;

    @GetMapping("/{parentId}/mypage")
    public ApiResponse<ParentResponseDTO.getMyPageDTO> getMyPage(@PathVariable("parentId") Long parentId) {
        ParentResponseDTO.getMyPageDTO parentResponseDTO = parentService.getMyPage(parentId);
        return ApiResponse.onSuccess(parentResponseDTO);
    }
}
