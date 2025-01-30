package com.ibridge.service;

import com.ibridge.domain.dto.response.QuestionBoardResponseDTO;
import com.ibridge.repository.QuestionBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionBoardService {
    private final QuestionBoardRepository questionBoardRepository;

    @Autowired
    public QuestionBoardService(QuestionBoardRepository questionBoardRepository) {
        this.questionBoardRepository = questionBoardRepository;
    }

    public QuestionBoardResponseDTO getQuestionBoard(Long parentId, int page) {
        return questionBoardRepository.findQuestionByParentId(parentId, page);
    }
}
