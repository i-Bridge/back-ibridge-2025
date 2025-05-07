package com.ibridge.service;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.domain.entity.Analysis;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.domain.entity.Subject;
import com.ibridge.repository.AnalysisRepository;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import com.ibridge.repository.SubjectRepository;
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
    private final SubjectRepository subjectRepository;

    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        boolean isCompleted = false;
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        if(todaySubject.size() > 1) isCompleted = true;
        else {
            List<Question> questionsBySubject = questionRepository.findBySubjectIdAndChildId(todaySubject.get(0).getId(), childId);
            if(questionsBySubject.size() > 0) isCompleted = true;
        }

        return ChildResponseDTO.getQuestionDTO.builder()
                .isCompleted(isCompleted)
                .question(todaySubject.get(0).getTitle()).build();
    }

    public ChildResponseDTO.getQuestionDTO getNextQuestion(Long childId, ChildRequestDTO.AnswerDTO request) {
        //ai와 연결

        return null;
    }
}
