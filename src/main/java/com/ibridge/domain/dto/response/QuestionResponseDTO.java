package com.ibridge.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

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
