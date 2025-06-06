package com.ibridge.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChildRequestDTO {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnswerDTO {
        Long subjectId;
        String text;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetPresignedURLDTO {
        String type;
        Long subjectId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UploadedDTO {
        Long subjectId;
        String image;
        String video;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FinishedDTO {
        Long subjectId;
    }
}