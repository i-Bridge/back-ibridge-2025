package com.ibridge.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StartSigninRequestDTO {
    private String email;
    private String name;
}
