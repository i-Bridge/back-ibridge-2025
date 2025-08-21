package com.ibridge.domain.dto.response;

import com.ibridge.domain.entity.Emotion;
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
    private Long cumulative;
    private List<Emotion> emotions;
    private List<Long> cumList;
    private List<KeywordDTO> keywords;
}
