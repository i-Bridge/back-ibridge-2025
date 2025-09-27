package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.SubjectDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParentHomeResponseDTO {
    private Integer emotion;
    private String negativeCategory;
    private String positiveCategory;
    private String mostTalkedCategory;
    private Integer cumulativeAnswerCount;
    private List<SubjectDTO> subjects;
    private String name;
    private Integer newGrape;
}
