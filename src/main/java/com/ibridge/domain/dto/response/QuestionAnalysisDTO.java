package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.QuestionDTO;
import com.ibridge.domain.dto.SubjectDTO;
import lombok.*;

import java.util.List;

@Getter
@Builder
public class QuestionAnalysisDTO {
    private SubjectDTO subjects;
    private List<QuestionDTO> questions;
}

