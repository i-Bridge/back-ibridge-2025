package com.ibridge.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionBoardResponseDTO {
    @JsonProperty("page")
    private int page;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("questions")
    private List<QuestionDTO> questions;

    @Getter
    @AllArgsConstructor
    public static class QuestionDTO {
        @JsonProperty("questionId")
        private Long questionId;

        @JsonProperty("text")
        private String text;

        @JsonProperty("time")
        private String time;

        @JsonProperty("child")
        private Long child;
    }
}
