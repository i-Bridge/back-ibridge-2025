package com.ibridge.service;

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

    public ChildResponseDTO.getHomeDTO getHome(Long childId) {
        Child child = childRepository.findById(childId).get();

        List<Question> questions = questionRepository.findByChildAndDate(child, LocalDate.now());
        List<ChildResponseDTO.getQuestionsDTO> questionList = new ArrayList<>();
        for(Question question : questions) {
            ChildResponseDTO.getQuestionsDTO temp = new ChildResponseDTO.getQuestionsDTO();
            temp.setQuestionId(question.getId());
            Optional<Analysis> analysis = analysisRepository.findByChildIdAndQuestionId(childId, question.getId());
            temp.setAnswer(analysis.isPresent());
            questionList.add(temp);
        }
        return ChildResponseDTO.getHomeDTO.builder().questions(questionList).build();
    }

}
