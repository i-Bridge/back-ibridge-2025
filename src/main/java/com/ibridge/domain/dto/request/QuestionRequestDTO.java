package com.ibridge.domain.dto.request;

import lombok.*;

@Getter
public class QuestionRequestDTO {
    private String question;
    private String time;
    private int period;
}
