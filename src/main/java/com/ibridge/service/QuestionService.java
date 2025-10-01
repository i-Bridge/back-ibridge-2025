package com.ibridge.service;

import com.ibridge.domain.dto.AnalysisDTO;
import com.ibridge.domain.dto.QuestionDTO;
import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.dto.request.EditQuestionRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final NoticeRepository noticeRepository;
    private final AnalysisRepository analysisRepository;
    private final Random random = new Random();
    private final ChildRepository childRepository;

    public QuestionAnalysisDTO getQuestionAnalysis(Parent parent, Long childId, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));

        if (!subject.getChild().getId().equals(childId)) {
            throw new AccessDeniedException("Subject does not belong to the given child.");
        }

        List<Question> questions = questionRepository.findAllBySubject(subject);
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        noticeRepository.findBySubjectAndReceiver(subject, parent)
                .ifPresent(noticeRepository::delete);
        for (Question q : questions) {
            Analysis analysis = q.getAnalysis();
            String video = analysis != null ? analysis.getVideo() : null;
            String image = analysis != null ? analysis.getImage() : null;
            String answer = analysis != null ? analysis.getAnswer() : null;
            questionDTOs.add(new QuestionDTO(q.getId(), q.getText(), video, image, answer));
        }

        return QuestionAnalysisDTO.builder()
                .subjects(new SubjectDTO(subject.getId(), subject.getTitle(), subject.isAnswer(), subject.getDate()))
                .questions(questionDTOs)
                .build();
    }


    public QuestionDetailResponseDTO getQuestionDetail(Long childId, Long subjectId, Long questionId, LocalDate date) {
        Analysis analysis = analysisRepository.findByQuestionId(questionId)
                .orElse(null); // 분석이 없을 수도 있음
        AnalysisDTO analysisDTO = analysis != null
                ? new AnalysisDTO(analysis.getId(), analysis.getAnswer())
                : null;

        return QuestionDetailResponseDTO.builder()
                .analysis(analysisDTO)
                .build();
    }

    public void editQuestion(Long childId, EditQuestionRequestDTO request, Long subjectId) {
        Subject subject = subjectRepository.findBySubjectId(subjectId);
        subject.setTitle(request.getTitle());
        subject.setDate(LocalDate.now());
        subjectRepository.save(subject);
        Question question = questionRepository.findBySubjectId(subjectId);
        question.setText(request.getTitle());
        questionRepository.save(question);
    }

    public SubjectResponseDTO rerollQuestion(Long childId, Long subjectId) {
        List<String> subjects = new ArrayList<>();
        try (InputStream inputStream = QuestionService.class.getClassLoader().getResourceAsStream("SubjectList.txt")) {
            if (inputStream == null) {
                System.out.println("파일을 찾을 수 없습니다.");
                throw new RuntimeException();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                subjects.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int id = random.nextInt(subjects.size());
        // 랜덤으로 하나 선택
        String randomQuestion = subjects.get(id);
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));
        subjectRepository.save(subject);
        Question question=questionRepository.findBySubjectId(subject.getId());
        question.setText(randomQuestion);
        questionRepository.save(question);
        return new SubjectResponseDTO(subjectId, randomQuestion);
    }

    public ScheduledDTO getScheduled(Long childId) {
        List<SubjectDTO> subjects = subjectRepository.findSubjectsByChildIdAndDate(childId, LocalDate.now()).stream()
                .map(subject -> new SubjectDTO(subject.getSubjectId(), subject.getSubjectTitle(), subject.isAnswer() ,subject.getDate()))
                .collect(Collectors.toList());

        return ScheduledDTO.builder()
                .subjects(subjects)
                .build();
    }
}
