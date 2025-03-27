package com.ibridge.service;

import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.dto.response.QuestionDTO;
import com.ibridge.domain.dto.response.SubjectDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.domain.entity.Subject;
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

    @Autowired
    public QuestionService(QuestionRepository questionRepository, SubjectRepository subjectRepository) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
    }

    public QuestionAnalysisDTO getQuestionAnalysis(Long childId, Long subjectId, LocalDate date) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 과목이 존재하지 않습니다."));

        List<QuestionDTO> questions = questionRepository.findBySubjectIdAndChildIdAndDate(subjectId, childId, date).stream()
                .map(q -> new QuestionDTO(q.getId(), q.getText()))
                .collect(Collectors.toList());

        return QuestionAnalysisDTO.builder()
                .subjects(Collections.singletonList(new SubjectDTO(subject.getId(), subject.getTitle())))
                .questions(questions)
                .build();
    }


}
