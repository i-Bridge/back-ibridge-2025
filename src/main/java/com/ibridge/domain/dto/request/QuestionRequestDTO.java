package com.ibridge.domain.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestionRequestDTO {
    private String question;

    @Builder
    public QuestionRequestDTO(String question) {
        this.question = question;
    }
}
