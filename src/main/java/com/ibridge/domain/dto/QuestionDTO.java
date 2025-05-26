package com.ibridge.domain.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class QuestionDTO {
    private Long questionId;
    private String text;
    private String video;
    private String image;
    private String answer;
}
