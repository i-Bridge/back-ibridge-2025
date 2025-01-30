package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionAnalysisDTO {
    private String video;
    private String result;
    @Builder
    public QuestionAnalysisDTO(String video, String result) {
        this.video = video;
        this.result = result;
    }
}

