package com.ibridge.service;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.domain.entity.Analysis;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Question;
import com.ibridge.domain.entity.Subject;
import com.ibridge.repository.AnalysisRepository;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.QuestionRepository;
import com.ibridge.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChildService {
    private final ChildRepository childRepository;
    private final QuestionRepository questionRepository;
    private final AnalysisRepository analysisRepository;
    private final SubjectRepository subjectRepository;
    private final S3Service s3Service;
    private final GptService gptService;

    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        boolean isCompleted = todaySubject.get(0).isAnswer();

        return ChildResponseDTO.getQuestionDTO.builder()
                .isCompleted(isCompleted).build();
    }

    public ChildResponseDTO.getPredesignedQuestionDTO getPredesignedQuestion(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        return ChildResponseDTO.getPredesignedQuestionDTO.builder()
                .question(todaySubject.get(0).getTitle())
                .subjectId(todaySubject.get(0).getId()).build();
    }

    public ChildResponseDTO.getNewQuestionDTO getNewSubject(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());

        for(Subject subject : todaySubject) {
            if (!subject.isAnswer()) {
                List<Question> tempQuestions = subject.getQuestions();
                questionRepository.delete(tempQuestions.get(tempQuestions.size() - 1));
            }
            subject.setAnswer(true);
            subjectRepository.save(subject);
        }

        Subject newSubject = Subject.builder()
                .child(childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found ")))
                .title("아이의 얘기 " + todaySubject.size())
                .date(LocalDate.now())
                .isAnswer(false).build();
        subjectRepository.save(newSubject);

        Question question = Question.builder()
                .subject(newSubject)
                .text("무슨 이야기를 하고 싶어?").build();
        questionRepository.save(question);

        return ChildResponseDTO.getNewQuestionDTO.builder()
                .subjectId(newSubject.getId()).build();
    }

    public ChildResponseDTO.getAI getNextQuestion(ChildRequestDTO.AnswerDTO request) {
        Subject targetSubject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject " + request.getSubjectId() + " Not Found "));
        List<Question> questions = questionRepository.findAllBySubject(targetSubject);
        Analysis analysis = Analysis.builder()
                .question(questions.get(questions.size() - 1))
                .answer(request.getText())
                .uploaded(false).build();
        analysisRepository.save(analysis);

        if(questions.size() == 5) {
            targetSubject.setAnswer(true);
            subjectRepository.save(targetSubject);

            return ChildResponseDTO.getAI.builder()
                    .isFinished(true).build();
        }
        else {
            String record = "";
            for(Question question : questions) {
                record += question + "\n" + analysisRepository.findByQuestionId(question.getId()).get().getAnswer() + "\n";
            }
            String ai = gptService.askGpt(record);
            Question question = Question.builder()
                    .text(ai)
                    .subject(targetSubject)
                    .build();
            questionRepository.save(question);

            return ChildResponseDTO.getAI.builder()
                    .isFinished(false)
                    .ai(ai).build();
        }
    }

    //s3 저장 경로 양식 : {childId}/{subjectId}/{yyyymmdd_hhmmss}.webm / {childId}/{subjectId}/{yyyymmdd_hhmmss}.jpeg
    public ChildResponseDTO.getPresignedURLDTO getPresignedURL(Long childId, ChildRequestDTO.GetPresignedURLDTO request) {
        LocalDateTime sended = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedSended = sended.format(formatter);
        String contentType = "";
        String objectKey = childId + "/" + request.getSubjectId() + "/" + formattedSended;

        if(request.getType().equals("video")) {
            contentType = "video/webm";
            objectKey += ".webm";
        }
        else if(request.getType().equals("image")) {
            contentType = "image/jpeg";
            objectKey += ".jpeg";
        }

        return ChildResponseDTO.getPresignedURLDTO.builder()
                .url(s3Service.generatePresignedUrl(objectKey, contentType, 600)).build();
    }

    public void uploaded(ChildRequestDTO.UploadedDTO request) {
        List<Question> questions = questionRepository.findAllBySubject(subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject " + request.getSubjectId() + " Not Found ")));
        for(Question question : questions) {
            Analysis analysis = analysisRepository.findByQuestionId(question.getId()).orElseThrow(() -> new RuntimeException("Question " + question.getId() + " Not Found "));
            if(!analysis.isUploaded()) {
                analysis.setVideo(request.getVideo());
                analysis.setImage(request.getImage());
                analysis.setUploaded(true);
                analysisRepository.save(analysis);
                break;
            }
        }
    }
}
