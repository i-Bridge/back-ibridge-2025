package com.ibridge.service;

import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.request.QuestionUpdateRequestDTO;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
                .time(Timestamp.valueOf(request.getTime() + " 00:00:00"))
                .build();

        Question savedQuestion = questionRepository.save(question);
        return new QuestionResponseDTO(savedQuestion.getId());
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
                .period(7)  // 임시 값 설정
                .build();
    }
    @Transactional
    public void updateQuestion(Long childId, Long questionId, QuestionUpdateRequestDTO requestDTO) {
        Question question = questionRepository.findByIdAndChild_Id(questionId, childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 질문을 찾을 수 없습니다."));

        question.setText(requestDTO.getQuestion());
        question.setType(requestDTO.getType());
        question.setTime(Timestamp.valueOf(LocalDateTime.now())); // 임시로 현재 시간 저장

        questionRepository.save(question);
    }

    @Transactional
    public List<QuestionResponseDTO> addRegularQuestion(Long childId, QuestionRequestDTO requestDTO) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 자녀를 찾을 수 없습니다."));

        if (requestDTO != null && requestDTO.getQuestion() != null) {
            Question question = Question.builder()
                    .text(requestDTO.getQuestion())
                    .time(new Timestamp(System.currentTimeMillis())) // 현재 시간 저장
                    .child(child)
                    .type(0) // 정기 질문 0
                    .build();

            questionRepository.save(question);
        }

        List<Question> questions = questionRepository.findByChild_IdAndType(childId, 1);
        return questions.stream()
                .map(q -> new QuestionResponseDTO(q.getId(), q.getText(), q.getTime().toString()))
                .collect(Collectors.toList());
    }
}
