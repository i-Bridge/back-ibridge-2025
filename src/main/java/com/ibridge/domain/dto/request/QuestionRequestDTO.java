package com.ibridge.domain.dto.request;

import lombok.*;

@Getter
public class QuestionRequestDTO {
    private String question;

    @Builder
    public QuestionRequestDTO(String question) {
        this.question = question;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class QuestionUpdateRequestDTO {
        private String question;
    }
}
