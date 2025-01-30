package com.ibridge.service;

import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.entity.Question;
import com.ibridge.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
