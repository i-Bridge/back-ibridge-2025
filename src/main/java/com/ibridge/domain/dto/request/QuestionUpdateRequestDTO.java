package com.ibridge.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionUpdateRequestDTO {
    private String question;
    private int type;
    private String time;
    private int period;
}
