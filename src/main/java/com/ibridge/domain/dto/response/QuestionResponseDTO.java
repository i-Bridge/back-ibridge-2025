package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestionResponseDTO {
    private Long QuestionId;

    @Builder
    public QuestionResponseDTO(Long QuestionId) {
        this.QuestionId = QuestionId;
    }
}
