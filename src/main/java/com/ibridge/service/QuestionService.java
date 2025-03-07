package com.ibridge.service;

import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
import com.ibridge.domain.dto.response.QuestionBoardResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.sql.Timestamp;
import java.util.ArrayList;
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

    @Transactional
    public QuestionResponseDTO addTempQuestion(Long childId, QuestionRequestDTO request) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 아이를 찾을 수 없습니다."));

        Question question = Question.builder()
                .child(child)
                .text(request.getQuestion())
                .time(request.getTime())
                .build();

        Question savedQuestion = questionRepository.save(question);
        return QuestionResponseDTO.builder().questionId(savedQuestion.getId()).build();
    }

    @Transactional(readOnly = true)
    public QuestionResponseDTO getQuestionForEdit(Long childId, Long questionId) {
        Question question = questionRepository.findByIdAndChild_Id(questionId, childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 질문을 찾을 수 없습니다."));

        return QuestionResponseDTO.builder()
                .questionId(question.getId())
                .child(question.getChild().getId())
                .text(question.getText())
                .time("오전")
                .type(question.getType())
                .date(question.getDate())
                .period(7)  // 임시 값 설정
                .build();
    }
    @Transactional
    public void updateQuestion(Long childId, Long questionId, QuestionUpdateRequestDTO requestDTO) {
        Question question = questionRepository.findByIdAndChild_Id(questionId, childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 질문을 찾을 수 없습니다."));

        question.setText(requestDTO.getQuestion());
        question.setType(requestDTO.getType());
        question.setTime(requestDTO.getTime());

        questionRepository.save(question);
    }

    @Transactional
    public void addRegularQuestion(Long childId, QuestionRequestDTO requestDTO) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 자녀를 찾을 수 없습니다."));

        Question question = Question.builder()
                .text(requestDTO.getQuestion())
                .time(requestDTO.getTime())
                .child(child)
                .date(new Timestamp(System.currentTimeMillis()))
                .type(0) // 정기 질문 0
                .build();

        questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getRegularQuestions(Long childId) {
        if (!childRepository.existsById(childId)) {
            throw new EntityNotFoundException("해당 자녀를 찾을 수 없습니다.");
        }
        List<Question> questions = questionRepository.findByChildId(childId);
        List<QuestionResponseDTO> data = new ArrayList<>();
        for(Question question : questions) {
            QuestionResponseDTO temp = QuestionResponseDTO.builder()
                    .questionId(question.getId())
                    .text(question.getText())
                    .time(question.getTime())
                    .build();
            data.add(temp);
        }
        return data;
    }

    @Transactional
    public void deleteQuestion(Long childId, Long questionId) {
        if (!childRepository.existsById(childId)) {
            throw new EntityNotFoundException("해당 자녀를 찾을 수 없습니다.");
        }

        // 해당 questionId가 childId에 속하는지 확인 후 삭제
        Question question = questionRepository.findByIdAndChild_Id(questionId, childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 질문을 찾을 수 없습니다."));

        questionRepository.delete(question);
    }

    public QuestionBoardResponseDTO getQuestionBoard(Long childId, int page, int size) {
        if (!childRepository.existsById(childId)) {
            throw new EntityNotFoundException("해당 자녀를 찾을 수 없습니다.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Question> questionPage = questionRepository.findAllByChildId(childId, pageable);

        List<QuestionResponseDTO> questionList = questionPage.getContent().stream()
                .map(q -> QuestionResponseDTO.builder()
                        .questionId(q.getId())
                        .text(q.getText())
                        .time(q.getTime())
                        .type(q.getType())
                        .period(q.getPeriod())
                        .child(q.getChild().getId())
                        .build())
                .toList();

        return QuestionBoardResponseDTO.builder()
                .page(questionPage.getNumber() + 1)
                .totalPages(questionPage.getTotalPages())
                .questions(questionList)
                .build();
    }
}
