package com.ibridge.service;

import com.ibridge.domain.dto.request.EditQuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.entity.Analysis;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.domain.entity.Subject;
import com.ibridge.repository.AnalysisRepository;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import com.ibridge.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final AnalysisRepository analysisRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, SubjectRepository subjectRepository, AnalysisRepository analysisRepository) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.analysisRepository = analysisRepository;
    }

    public QuestionAnalysisDTO getQuestionAnalysis(Long childId, Long subjectId, LocalDate date) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 과목이 존재하지 않습니다."));

        List<QuestionDTO> questions = questionRepository.findBySubjectId(subjectId).stream()
                .map(q -> new QuestionDTO(q.getId(), q.getText()))
                .collect(Collectors.toList());

        return QuestionAnalysisDTO.builder()
                .subjects(Collections.singletonList(new SubjectDTO(subject.getId(), subject.getTitle())))
                .questions(questions)
                .build();
    }

    public QuestionDetailResponseDTO getQuestionDetail(Long childId, Long subjectId, Long questionId, LocalDate date) {
        Question question = questionRepository.findByIdAndSubjectId(questionId, subjectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 질문이 존재하지 않습니다."));

        Analysis analysis = analysisRepository.findByQuestionId(questionId)
                .orElse(null); // 분석이 없을 수도 있음

        QuestionDTO questionDTO = new QuestionDTO(question.getId(), question.getText());
        AnalysisDTO analysisDTO = analysis != null
                ? new AnalysisDTO(analysis.getId(), analysis.getAnswer())
                : null;

        return QuestionDetailResponseDTO.builder()
                .question(questionDTO)
                .analysis(analysisDTO)
                .build();
    }

    @Transactional
    public void editQuestion(Long childId, EditQuestionRequestDTO request) {
        Question question = questionRepository.findByChildId(childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이의 질문을 찾을 수 없습니다."));

        question.setText(request.getTitle());
        questionRepository.save(question);
    }
}
