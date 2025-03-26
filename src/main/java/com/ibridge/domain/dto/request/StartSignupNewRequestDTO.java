package com.ibridge.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Getter
@NoArgsConstructor
public class StartSignupNewRequestDTO {

    @NotNull
    private List<ChildRequest> children;

    @Getter
    @NoArgsConstructor
    public static class ParentRequest {
        private String familyName;
    }

    @Getter
    @NoArgsConstructor
    public static class ChildRequest {
        private String name;
        private Integer gender;
        private String birth;
    }
}
