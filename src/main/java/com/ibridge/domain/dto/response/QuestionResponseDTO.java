package com.ibridge.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseDTO {
    private Long questionId;
    private Long child;
    private String text;
    private String time;
    private int type;
    private int period;

    public QuestionResponseDTO(Long id) {
        this.questionId = id;

    }

    public QuestionResponseDTO(Long id, String text, String string) {
        this.questionId = id;
        this.text = text;
        this.time = string;
    }
}
