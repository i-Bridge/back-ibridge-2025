package com.ibridge.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@NoArgsConstructor
public class StartRequestDTO {
    @NotNull
    private String FamilyName;
}
