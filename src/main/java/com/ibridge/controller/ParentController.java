package com.ibridge.controller;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.service.ParentService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

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

    @PatchMapping("/{parentId}/mypage/edit")
    public ApiResponse<ParentResponseDTO.EditInfo> EditInfo(@PathVariable("parentId") Long parentId, @RequestBody ParentRequestDTO.EditInfo editInfo) throws ParseException {
        ParentResponseDTO.EditInfo editInfoDTO = parentService.editInfo(parentId, editInfo);
        return ApiResponse.onSuccess(editInfoDTO);
    }
}
