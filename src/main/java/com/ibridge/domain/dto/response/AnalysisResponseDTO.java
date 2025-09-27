package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResponseDTO {
    private String name;
    private LocalDate signupDate;
    private Long cumulative;
    private List<Integer> emotions;
    private List<Long> cumList;
    private List<KeywordDTO> keywords;
}
