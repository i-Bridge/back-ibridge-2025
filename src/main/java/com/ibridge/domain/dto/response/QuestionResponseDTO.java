package com.ibridge.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
public class QuestionResponseDTO {
    private Long questionId;

    @Builder
    public QuestionResponseDTO(Long questionId) {
        this.questionId = questionId;
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

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeletedQuestionResponse {
        @JsonProperty("questionId")
        private Long questionId;

        @JsonProperty("deletedAt")
        private String deletedAt;
    }
}
