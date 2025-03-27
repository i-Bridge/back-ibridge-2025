package com.ibridge.service;

import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.entity.Analysis;
import com.ibridge.repository.AnalysisRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {
    private final AnalysisRepository analysisRepository;

    @Autowired
    public AnalysisService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

}
