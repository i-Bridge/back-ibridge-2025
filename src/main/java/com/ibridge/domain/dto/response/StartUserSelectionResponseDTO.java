package com.ibridge.domain.dto.response;

import com.ibridge.domain.entity.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Builder
public class StartUserSelectionResponseDTO {
    private boolean isAccepted;
    private boolean isSend;
    private String familyName;
    private List<ChildDTO> children;

    @Getter
    @Builder
    public static class ChildDTO {
        private Long id;
        private String name;
        private LocalDate birth;
        private Gender gender;
    }
}
