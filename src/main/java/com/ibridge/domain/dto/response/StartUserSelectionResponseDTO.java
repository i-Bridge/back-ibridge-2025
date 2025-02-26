package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StartUserSelectionResponseDTO {
    private boolean status; // false: 수락 못받음, true: 수락 받음
    private List<ParentDTO> parents;
    private List<ChildDTO> children;

    @Getter
    @Builder
    public static class ParentDTO {
        private Long id;
        private String name;
        private String relation;
    }

    @Getter
    @Builder
    public static class ChildDTO {
        private Long id;
        private String name;
        private String birth;
        private int gender; // 0: 남자, 1: 여자
    }
}
