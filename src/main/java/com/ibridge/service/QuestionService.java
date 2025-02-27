package com.ibridge.service;

import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
import com.ibridge.domain.dto.response.QuestionEditResponseDTO;
import com.ibridge.domain.dto.response.QuestionListResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final ChildRepository childRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, ChildRepository childRepository) {
        this.questionRepository = questionRepository;
        this.childRepository = childRepository;
    }

    public QuestionListResponseDTO createQuestion(Long parentId, QuestionUpdateRequestDTO request) {
        questionRepository.saveQuestion(parentId, request.getQuestion());
        List<QuestionListResponseDTO.QuestionDTO> questions = questionRepository.findQuestionsByParentId(parentId);
        return new QuestionListResponseDTO(questions);
    }

    public QuestionResponseDTO.DeletedQuestionResponse deleteQuestion(Long parentId, Long questionId) {
        questionRepository.deleteQuestion(parentId, questionId);
        return new QuestionResponseDTO.DeletedQuestionResponse(questionId, LocalDate.now().toString());
    }

    @Transactional
    public QuestionResponseDTO addTempQuestion(Long childId, QuestionRequestDTO request) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 아이를 찾을 수 없습니다."));

        Question question = Question.builder()
                .child(child)
                .text(request.getQuestion())
                .time(Timestamp.valueOf(request.getTime() + " 00:00:00"))
                .build();

        Question savedQuestion = questionRepository.save(question);
        return new QuestionResponseDTO(savedQuestion.getId());
    }

    @Transactional
    public QuestionEditResponseDTO updateQuestion(Long parentId, Long questionId, QuestionUpdateRequestDTO request) {
        Question question = questionRepository.findByIdAndChild_ParentId(questionId, parentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 질문을 찾을 수 없습니다."));

        question.setText(request.getQuestion());
        question.setTime(request.getTime());
        question.setType(request.getType());

        return QuestionEditResponseDTO.builder()
                .questionId(question.getId())
                .child(question.getChild().getId())
                .text(question.getText())
                .time(question.getTime().toString())
                .type(question.getType())
                .period(7)  // 임시 값 설정
                .build();
    }
}
