package com.ibridge.domain.dto.response;

import com.ibridge.domain.entity.PII;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PIIResponseDTO {
    private String consentToCollection;
    private String consentToService;
    private String consentToMarketing;

    public PIIResponseDTO(String s1, String s2, String s3) {
        consentToCollection = s1;
        consentToService = s2;
        consentToMarketing = s3;
    }
}
