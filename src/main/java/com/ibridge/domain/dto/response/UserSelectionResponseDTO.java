package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserSelectionResponseDTO {
    private List<ParentInfo> parents;
    private List<ChildInfo> children;

    @Getter
    @AllArgsConstructor
    public static class ParentInfo {
        private Long id;
        private String name;
        private String relation;
    }

    @Getter
    @AllArgsConstructor
    public static class ChildInfo {
        private Long id;
        private String name;
        private LocalDate birth;
        private int gender;
    }
}
