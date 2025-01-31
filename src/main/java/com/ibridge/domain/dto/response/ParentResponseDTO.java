package com.ibridge.domain.dto.response;

import lombok.*;

import java.util.Date;
import java.util.List;


public class ParentResponseDTO {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class getMyPageDTO {
        String name;
        String account;
        String birth;
        String relation;
        List<childDTO> children;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class childDTO {
        Long childId;
        String name;
        String birth;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EditInfo {
        Long parentId;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParentHome{
        public String today;
        public List<QuestionResponseDTO.QuestionResponse> questions;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteDTO {
        Date deletedAt;
        String account;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddChildDTO {
        Long childId;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PatchChildDTO {
        Long childId;
    }
}
