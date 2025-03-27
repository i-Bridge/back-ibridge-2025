package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubjectDTO {
    private Long subjectId;
    private String subjectTitle;
    private boolean isAnswer;
}
