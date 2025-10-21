package com.ibridge.domain.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmotionAnalysisResponseDTO {
    private LocalDate signupDate;
    private Integer emotion;
    private List<Integer> emotions;
}
