package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParentHomeResponseDTO {
    private List<AnswerCountDTO> answers;
    private List<QuestionDTO> questions;

    @Getter
    @Builder
    public static class AnswerCountDTO {
        private int count;
    }

    @Getter
    @Builder
    public static class QuestionDTO {
        private Long questionId;
        private String question;
        private int type;  // 0: 정기, 1: 주기, 2: 오늘
        private String time; // 정기 질문일 경우 "오전" 또는 "오후", 그 외 null
        private boolean isAnswer;
    }
}
