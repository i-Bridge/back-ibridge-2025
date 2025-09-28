package com.ibridge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SubjectDTO {
    private Long subjectId;
    private String subjectTitle;
    private boolean isAnswer;
    private LocalDate date;
}
