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
    private List<Integer> cumList;
    private List<keywordDTO> keywords;
    private List<Emotion> emotions;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class keywordDTO{
        private String keyword;
        private Long positiveScore;
    }
}
