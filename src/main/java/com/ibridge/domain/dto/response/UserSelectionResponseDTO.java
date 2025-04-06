package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
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
        private Date birth;
        private int gender;
    }
}
