package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QuestionEditResponseDTO {
    private Long questionId;
    private Long child;
    private String text;
    private String time;
    private int type;
    private int period;
}
