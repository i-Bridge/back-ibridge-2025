package com.ibridge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectDTO {
    private Long subjectId;
    private String subjectTitle;
    private boolean isAnswer;
    private LocalDate date;
}
