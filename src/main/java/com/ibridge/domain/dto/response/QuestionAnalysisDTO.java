package com.ibridge.domain.dto.response;

import com.ibridge.domain.entity.Analysis;
import com.ibridge.domain.entity.Question;
import lombok.*;

import java.util.List;

@Getter
@Builder
public class QuestionAnalysisDTO {
    private List<SubjectDTO> subjects;
    private List<QuestionDTO> questions;
}

