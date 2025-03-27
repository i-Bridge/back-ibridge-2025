package com.ibridge.service;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.domain.entity.Analysis;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.repository.AnalysisRepository;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChildService {

    private final ChildRepository childRepository;
    private final QuestionRepository questionRepository;
    private final AnalysisRepository analysisRepository;

    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        Child child = childRepository.findById(childId).get();

        return ChildResponseDTO.getQuestionDTO.builder().question(new Question().getText()).build();
    }

    public ChildResponseDTO.getQuestionDTO getNextQuestion(Long childId, ChildRequestDTO.AnswerDTO request) {
        return null;
    }
}
