package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BannerDTO {
    private Long cumulativeAnswerCount;
    private String mostTalkedCategory;
    private String positiveCategory;
    private String negativeCategory;
    private Integer emotion;
    private String name;
    private Integer newGrape;
}
