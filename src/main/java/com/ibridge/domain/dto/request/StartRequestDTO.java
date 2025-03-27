package com.ibridge.domain.dto.request;

import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartRequestDTO {
    @NotNull
    private String FamilyName;
}
