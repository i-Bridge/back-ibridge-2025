package com.ibridge.service;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        boolean isCompleted = false;
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        if(todaySubject.size() > 1) isCompleted = true;
        else {
            List<Question> questionsBySubject = questionRepository.findBySubjectIdAndChildId(todaySubject.get(0).getId(), childId);
            if(questionsBySubject.size() > 0) isCompleted = true;
        }

        return ChildResponseDTO.getQuestionDTO.builder()
                .isCompleted(isCompleted)
                .question(todaySubject.get(0).getTitle()).build();
    }

    public ChildResponseDTO.getAI getNextQuestion(Long childId, ChildRequestDTO.AnswerDTO request) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        String ai = gptService.askGpt(request.getText());

        if(request.getSubjectId() == 1) {
            List<Subject> subject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
            Question question = Question.builder()
                    .subject(subject.get(0))
                    .text(ai).build();
            questionRepository.save(question);

            Analysis analysis = Analysis.builder()
                    .question(question)
                    .uploaded(false).build();
            analysisRepository.save(analysis);

            return ChildResponseDTO.getAI.builder()
                    .ai(ai)
                    .id(analysis.getId()).build();
        }
        else {
            if(request.getSubjectId() == todaySubject.size()) {
                Subject target = todaySubject.get(todaySubject.size() - 1);

                Question question = Question.builder()
                        .text(ai)
                        .subject(target).build();
                questionRepository.save(question);

                Analysis analysis = Analysis.builder()
                        .question(question)
                        .uploaded(false)
                        .answer(request.getText()).build();
                analysisRepository.save(analysis);

                return ChildResponseDTO.getAI.builder()
                        .ai(ai)
                        .id(analysis.getId()).build();
            }
            else {
                Subject prevSubject = todaySubject.get(todaySubject.size() - 2);
                prevSubject.setAnswer(true);
                subjectRepository.save(prevSubject);

                Subject subject = Subject.builder()
                        .child(childRepository.findById(childId).get())
                        .title(ai)
                        .date(LocalDate.now())
                        .isAnswer(false).build();
                subjectRepository.save(subject);

                Question question = Question.builder()
                        .subject(subject)
                        .text(ai).build();
                questionRepository.save(question);

                Analysis analysis = Analysis.builder()
                        .question(question)
                        .answer(request.getText())
                        .uploaded(false).build();
                analysisRepository.save(analysis);

                return ChildResponseDTO.getAI.builder()
                        .id(analysis.getId())
                        .ai(ai).build();
            }
        }
    }

    //s3 저장 경로 양식 : {childId}/{analysisId}/{yyyymmdd_hhmmss}.webm / {childId}/{analysisId}/{yyyymmdd_hhmmss}.jpeg
    public ChildResponseDTO.getPresignedURLDTO getPresignedURL(Long childId, ChildRequestDTO.GetPresignedURLDTO request) {
        LocalDateTime sended = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedSended = sended.format(formatter);
        String contentType = "";
        String objectKey = childId + "/" + request.getId() + "/" + formattedSended;

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
        Analysis analysis = analysisRepository.findByQuestionId(request.getId()).orElseThrow(() -> new RuntimeException("해당하는 Id가 없습니다"));
        analysis.setVideo(request.getVideo());
        analysis.setImage(request.getImage());
        analysis.setUploaded(true);
        analysisRepository.save(analysis);
    }
}
