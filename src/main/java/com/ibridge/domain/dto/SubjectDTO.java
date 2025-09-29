package com.ibridge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectDTO {
    private Long subjectId;
    private String subjectTitle;
    private boolean isAnswer;
    private LocalDate date;
    private String image;

    public SubjectDTO(Long subjectId, String subjectTitle, boolean answer, LocalDate date) {
        this.subjectId = subjectId;
        this.subjectTitle = subjectTitle;
        this.isAnswer = answer;
        this.date = date;
    }
}
