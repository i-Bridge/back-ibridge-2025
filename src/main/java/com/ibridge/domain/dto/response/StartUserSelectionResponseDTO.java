package com.ibridge.domain.dto.response;

import com.ibridge.domain.entity.Gender;
import com.ibridge.domain.entity.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Builder
public class StartUserSelectionResponseDTO {
    private String familyName;
    private Status status;
    private List<ParentDTO> parents;
    private List<ChildDTO> children;

    @Getter
    @Builder
    public static class ParentDTO {
        private Long id;
        private String name;
    }
    @Getter
    @Builder
    public static class ChildDTO {
        private Long id;
        private String name;
        private LocalDate birth;
        private Gender gender;
    }
}
