package com.ibridge.domain.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectListResponseDTO {
    private int subjectCount;
    private List<SubjectResponseDTO> subjects;

    @Data
    @Builder
    public static class SubjectResponseDTO {
        private Long subjectId;
        private String title;
        private List<QuestionResponseDTO> questions;
    }
    @Data
    @Builder
    public static class QuestionResponseDTO {
        private String text;
        private String answer;
        private String image;
        private String video;
    }
}
