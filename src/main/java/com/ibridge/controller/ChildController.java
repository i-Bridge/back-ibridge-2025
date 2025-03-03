package com.ibridge.controller;

import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.service.ChildService;
import com.ibridge.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/child")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @GetMapping("/{childId}/home")
    public ApiResponse<ChildResponseDTO.getHomeDTO> home(@PathVariable Long childId) {
        ChildResponseDTO.getHomeDTO data = childService.getHome(childId);
        return ApiResponse.onSuccess(data);
    }
}
