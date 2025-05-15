package com.ibridge.service;

import com.ibridge.domain.dto.response.SubjectResponseDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.domain.entity.Subject;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import com.ibridge.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class OtherService {
    private final ChildRepository childRepository;
    private final QuestionService questionService;
    private final Random random = new Random();
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;

    public void setSubject() {
        List<Child> children = childRepository.findAll();
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
        String randomQuestion = subjects.get(id);
        for (Child child : children) {
            Subject todaySubject = Subject.builder()
                    .title(randomQuestion)
                    .child(child)
                    .date(LocalDate.now())
                    .isAnswer(false).build();
            subjectRepository.save(todaySubject);

            Question question = Question.builder()
                    .subject(todaySubject)
                    .text(randomQuestion).build();
            questionRepository.save(question);

            questionService.rerollQuestion(child.getId(), LocalDate.now());
        }
    }
}
