package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QuestionDetailResponseDTO {
    private AnalysisDTO analysis;
}
