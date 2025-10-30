package com.ibridge.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRequestDTO {
    private boolean requiredPII;
    private boolean optionalPII;
}
