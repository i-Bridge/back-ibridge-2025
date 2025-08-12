package com.ibridge.service;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    private final ParentRepository parentRepository;
    private final NoticeRepository NoticeRepository;

    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        boolean isCompleted = todaySubject.get(0).isCompleted();

        return ChildResponseDTO.getQuestionDTO.builder()
                .name(childRepository.findById(childId).orElseThrow(() -> new RuntimeException(childId + "인 child가 없습니다.")).getName())
                .isCompleted(isCompleted).build();
    }

    public ChildResponseDTO.getPredesignedQuestionDTO getPredesignedQuestion(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        List<Question> questions = questionRepository.findAllBySubject(todaySubject.get(0));
        Question question = questions.get(questions.size() - 1);

        return ChildResponseDTO.getPredesignedQuestionDTO.builder()
                .question(question.getText())
                .subjectId(todaySubject.get(0).getId()).build();
    }

    public ChildResponseDTO.getNewQuestionDTO getNewSubject(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        Subject newSubject = Subject.builder()
                .child(childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found ")))
                .title("아이의 얘기 " + (todaySubject.size()))
                .date(LocalDate.now())
                .isAnswer(false)
                .isCompleted(false).build();
        subjectRepository.save(newSubject);

        Question question = Question.builder()
                .subject(newSubject)
                .text("무슨 이야기를 하고 싶어?").build();
        questionRepository.save(question);

        return ChildResponseDTO.getNewQuestionDTO.builder()
                .subjectId(newSubject.getId()).build();
    }

    public ChildResponseDTO.getAI getNextQuestion(ChildRequestDTO.AnswerDTO request) {
        System.out.println("Requesting Next Question: " + request.getText());
        Subject targetSubject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject " + request.getSubjectId() + " Not Found "));
        targetSubject.setAnswer(true);
        List<Question> questions = questionRepository.findAllBySubject(targetSubject);
        Analysis analysis = Analysis.builder()
                .question(questions.get(questions.size() - 1))
                .answer(request.getText())
                .uploaded(false).build();
        analysisRepository.save(analysis);
        System.out.println("Analysis Saved: " + analysis.getId());

        if(questions.size() == 5) {
            System.out.println("Completely Finished with " + questions.get(questions.size()- 1).getId());
            targetSubject.setCompleted(true);
            String conv = "";
            for(Question question : questions) {
                conv += question.getText() + "\n" + analysisRepository.findByQuestionId(question.getId()).get().getAnswer() + "\n";
            }
            String summary = gptService.summarizeGPT(conv);
            targetSubject.setTitle(summary);
            subjectRepository.save(targetSubject);

            makeNotice(targetSubject);

            return ChildResponseDTO.getAI.builder()
                    .ai(gptService.closingGPT(conv))
                    .isFinished(true).build();
        }
        else {
            String record = "";
            for(Question question : questions) {
                record += question.getText() + "\n" + analysisRepository.findByQuestionId(question.getId()).get().getAnswer() + "\n";
            }
            String ai = gptService.askGpt(record);
            Question question = Question.builder()
                    .text(ai)
                    .subject(targetSubject)
                    .build();
            questionRepository.save(question);

            System.out.println("New Question: " + question.getId() + " / " + question.getText());

            return ChildResponseDTO.getAI.builder()
                    .isFinished(false)
                    .ai(ai).build();
        }
    }

    //s3 저장 경로 양식 : {childId}/{subjectId}/{yyyymmdd_hhmmss}.webm / {childId}/{subjectId}/{yyyymmdd_hhmmss}.jpeg
    public ChildResponseDTO.getPresignedURLDTO getPresignedURL(Long childId, ChildRequestDTO.GetPresignedURLDTO request) {
        System.out.println("Requesting PresignedURL: " + request.getSubjectId());
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
        System.out.println("Video Uploaded" + request.getVideo());
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

    public void answerFinished(Long childId, ChildRequestDTO.FinishedDTO request) {
        System.out.println("Requesting answer is finished in mid: " + request.getSubjectId());
        Subject predesigned = subjectRepository.findByChildIdAndDate(childId, LocalDate.now()).get(0);

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject " + request.getSubjectId() + " Not Found "));
        List<Question> questions = questionRepository.findAllBySubject(subject);
        List<Analysis> analysisList = new ArrayList<>();
        for(Question question : questions) {
            Analysis analysis = analysisRepository.findByQuestionId(question.getId()).orElse(null);
            if(analysis != null) analysisList.add(analysis);
        }

        if(predesigned == subject) {
            subject.setAnswer(true);
            subjectRepository.save(subject);

            return;
        }
        else if(questions.size() == 1) {
            System.out.println("questions' size == 1, deleted");
            questionRepository.delete(questions.get(0));
            subjectRepository.delete(subject);
            return;
        }
        else if(subject.isCompleted()) return;
        else {
            questionRepository.delete(questions.get(questions.size() - 1));
            questions.remove(questions.size() - 1);
            subject.setAnswer(true);

            System.out.println("Questions' Size: " + questions.size());
            String conv = "";
            for(Question question : questions) {
                conv += question.getText() + "\n" + analysisRepository.findByQuestionId(question.getId()).get().getAnswer() + "\n";
            }
            String summary = gptService.summarizeGPT(conv);
            subject.setTitle(summary);
            subjectRepository.save(subject);

            makeNotice(subject);
            return;
        }
    }

    private void makeNotice(Subject subject) {
        Child child = subject.getChild();
        Family family = child.getFamily();
        List<Parent> parents = parentRepository.findAllByFamily(family);

        for(Parent parent: parents) {
            Notice p = Notice.builder()
                    .child(child)
                    .send_at(Timestamp.valueOf(LocalDateTime.now()))
                    .type(1)
                    .receiver(parent)
                    .isRead(false)
                    .subject(subject)
                    .build();
            NoticeRepository.save(p);
        }
    }
}
