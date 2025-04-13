package com.ibridge.service;

import com.ibridge.domain.dto.AnalysisDTO;
import com.ibridge.domain.dto.QuestionDTO;
import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.dto.request.EditQuestionRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.entity.Analysis;
import com.ibridge.domain.entity.Question;
import com.ibridge.domain.entity.Subject;
import com.ibridge.repository.AnalysisRepository;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import com.ibridge.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final AnalysisRepository analysisRepository;
    private final Random random = new Random();
    private final ChildRepository childRepository;

    public QuestionAnalysisDTO getQuestionAnalysis(Long childId, Long subjectId, LocalDate date) {
        Subject subject = subjectRepository.findById(subjectId).get();
        List<Question> questions = questionRepository.findBySubjectIdAndChildId(subjectId, childId);
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for(Question q : questions) {
            Analysis analysis = analysisRepository.findByQuestionId(q.getId()).get();
            questionDTOs.add(new QuestionDTO(q.getId(), q.getText(), analysis.getVideo(), analysis.getAnswer()));
        }

        return QuestionAnalysisDTO.builder()
                .subjects(new SubjectDTO(subject.getId(), subject.getTitle(), subject.isAnswer()))
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

    public void editQuestion(Long childId, EditQuestionRequestDTO request, LocalDate date) {
        List<Subject> subjects = subjectRepository.findByChildIdAndDate(childId, date);
        subjectRepository.deleteByChildIdAndDate(childId, date);
        Subject subject = subjects.get(0);
        subject.setTitle(request.getTitle());
        subject.setDate(Timestamp.valueOf(LocalDateTime.now()));
        subjectRepository.save(subject);
    }

    public SubjectResponseDTO rerollQuestion(Long childId, LocalDate date) {
        List<String> subjects = new ArrayList<>();
        String filePath = "src/main/resources/SubjectList.txt";
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = br.readLine())!=null){
                subjects.add(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        int id = random.nextInt(subjects.size());
        // 랜덤으로 하나 선택
        String randomQuestion = subjects.get(id);
        subjectRepository.deleteByChildIdAndDate(childId, date);

        subjectRepository.save(new Subject((long)id, randomQuestion, false, Timestamp.valueOf(LocalDateTime.now()), childRepository.findById(childId).get(), null));
        return new SubjectResponseDTO((long)id, randomQuestion);
    }
}
