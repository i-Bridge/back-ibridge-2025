package com.ibridge.service;

import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.response.QuestionListResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Question;
import com.ibridge.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void updateQuestion(Long parentId, Long questionId, QuestionRequestDTO.QuestionUpdateRequestDTO request) {
        questionRepository.updateQuestion(parentId, questionId, request.getQuestion());
    }

    public QuestionListResponseDTO createQuestion(Long parentId, QuestionRequestDTO.QuestionUpdateRequestDTO request) {
        questionRepository.saveQuestion(parentId, request.getQuestion());
        List<QuestionListResponseDTO.QuestionDTO> questions = questionRepository.findQuestionsByParentId(parentId);
        return new QuestionListResponseDTO(questions);
    }

    public QuestionResponseDTO.DeletedQuestionResponse deleteQuestion(Long parentId, Long questionId) {
        questionRepository.deleteQuestion(parentId, questionId);
        return new QuestionResponseDTO.DeletedQuestionResponse(questionId, LocalDate.now().toString());
    }
}
