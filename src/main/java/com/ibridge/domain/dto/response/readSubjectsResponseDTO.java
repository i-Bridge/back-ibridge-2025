package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class readSubjectsResponseDTO {
    private String name;
    private List<Boolean> month;
    private Integer cumulativeAnswerCount;
    private String mostTalkedCategory;
    private String positiveCategory;
    private String negativeCategory;
    private Integer emotion;
}
