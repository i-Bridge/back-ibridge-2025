package com.ibridge.domain.dto.response;

import com.ibridge.domain.entity.Analysis;
import com.ibridge.domain.entity.Question;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionAnalysisDTO {
    private String video;
    private String result;

    public static QuestionAnalysisDTO from(Analysis entity) {
        return QuestionAnalysisDTO.builder()
                .video(entity.getVideo())
                .result(entity.getAnswer())
                .build();
    }
}

