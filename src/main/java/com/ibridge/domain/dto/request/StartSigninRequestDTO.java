package com.ibridge.domain.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StartSigninRequestDTO {
    private String email;
    private String name;
}
