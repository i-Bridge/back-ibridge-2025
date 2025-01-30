package com.ibridge.domain.dto.response;

import lombok.*;

@Getter
public class QuestionResponseDTO {
    private Long QuestionId;

    @Builder
    public QuestionResponseDTO(Long QuestionId) {
        this.QuestionId = QuestionId;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionResponse{
        public Long questionId;
        public String question;
        public boolean isAnswer;
    }
}
