package com.ibridge.domain.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class QuestionDTO {
    private Long questionId;
    private String text;
    private String video;
    private String answer;
}
