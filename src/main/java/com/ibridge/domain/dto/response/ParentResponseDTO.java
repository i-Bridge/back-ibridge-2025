package com.ibridge.domain.dto.response;

import com.ibridge.domain.entity.Relation;
import lombok.*;

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
}
