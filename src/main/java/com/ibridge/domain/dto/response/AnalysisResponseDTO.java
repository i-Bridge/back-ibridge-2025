package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResponseDTO {
    private List<Integer> emotions;
    private List<Long> cumList;
    private List<KeywordDTO> keywords;
}
