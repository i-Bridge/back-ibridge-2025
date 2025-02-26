package com.ibridge.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

public class ParentRequestDTO {
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EditInfo {
        String name;
        String birthday;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddChildDTO {
        String name;
        String birthday;
        int gender;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EditChildDTO {
        Long childId;
        String name;
        String birthday;
        int gender;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteChildDTO {
        Long childId;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class getParentintoFamilyDTO{
        Long parentId;
    }
}
