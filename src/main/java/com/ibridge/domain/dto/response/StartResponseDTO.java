package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.request.StartRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StartResponseDTO {
    private boolean isFirst;
}
