package com.ibridge.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

public class ChildRequestDTO {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnswerDTO {
        MultipartFile video;
    }

}
