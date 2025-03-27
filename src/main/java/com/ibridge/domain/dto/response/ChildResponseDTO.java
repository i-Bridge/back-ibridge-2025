package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class ChildResponseDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getQuestionDTO {
        String question;
    }
}
