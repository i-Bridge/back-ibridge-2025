package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StartSignInDTO {
    private boolean isFirst;
}
