package com.ibridge.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
