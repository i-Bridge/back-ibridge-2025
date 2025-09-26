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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ChildRefactoringService {
    private final SubjectRepository subjectRepository;
    private final ChildRepository childRepository;
    private final ChildStatRepository childStatRepository;
    private final QuestionRepository questionRepository;
    private final GptService gptService;
    private final AnalysisRepository analysisRepository;
    private final S3Service s3Service;
    private final ParentRepository parentRepository;
    private final NoticeRepository noticeRepository;

    //질문 화면 관련
    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child not found"));
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        ChildStat dailyStat = childStatRepository.findDateStatByChildandToday(child, LocalDate.now());

        return ChildResponseDTO.getQuestionDTO.builder()
                .grape(child.getGrape())
                .emotion(dailyStat.getEmotion() != null)
                .isCompleted(todaySubject.get(0).isCompleted()).build();
    }

    public void setEmotion(Long childId, ChildRequestDTO.setEmotionDTO request) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child not found"));

        ChildStat dailyStat = childStatRepository.findDateStatByChildandToday(child, LocalDate.now());
        dailyStat.setEmotion(request.getEmotion());
        childStatRepository.save(dailyStat);
    }

    public ChildResponseDTO.getPredesignedQuestionDTO getPredesignedQuestion(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child not found"));

        Subject baseSubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now()).get(0);
        List<Question> questions = questionRepository.findAllBySubject(baseSubject);
        Question lastQuestion = questions.get(questions.size() - 1);

        return ChildResponseDTO.getPredesignedQuestionDTO.builder()
                .subjectId(baseSubject.getId())
                .question(lastQuestion.getText()).build();
    }

    public ChildResponseDTO.getNewQuestionDTO getNewSubject(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child not found"));

        Subject newSubject = Subject.builder()
                .child(child)
                .title("아이의 얘기")
                .date(LocalDate.now())
                .isAnswer(false)
                .isCompleted(false).build();
        subjectRepository.save(newSubject);

        Question question = Question.builder()
                .text("무슨 이야기를 하고 싶어?")
                .subject(newSubject).build();
        questionRepository.save(question);

        return ChildResponseDTO.getNewQuestionDTO.builder()
                .subjectId(newSubject.getId()).build();
    }

    public ChildResponseDTO.getAI getNextQuestion(Long childId, ChildRequestDTO.AnswerDTO request) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Question> questions = questionRepository.findAllBySubject(subject);
        Analysis answer = Analysis.builder()
                .question(questions.get(questions.size() - 1))
                .answer(request.getText())
                .uploaded(false).build();
        analysisRepository.save(answer);

        //지금까지의 대화
        StringBuilder sb = new StringBuilder(2048);
        for (Question question : questions) {
            sb.append(question.getText()).append('\n')
                    .append(analysisRepository.findByQuestionId(question.getId()).get().getAnswer()).append('\n');
        }
        String conv = sb.toString();

        switch (questions.size()) {
            // 대답 완료
            case 5:
                //완료 처리
                subject.setCompleted(true);

                //요약 후 subject 이름 변경
                String summary = gptService.summarizeGPT(conv);
                subject.setTitle(summary);

                //긍부정 평가
                Map<String, Integer> positiveRate = gptService.positiveGPT(conv);
                int positive = positiveRate.get("positive");
                subject.setPositive(positive);

                subjectRepository.save(subject);

                makeNotice(subject);

                return ChildResponseDTO.getAI.builder()
                        .ai(gptService.closingGPT(conv))
                        .isFinished(true).build();

            // 대답 완료 이전
            default:
                String ai = gptService.askGpt(conv);
                Question newQuestion = Question.builder()
                        .subject(subject)
                        .text(ai).build();
                questionRepository.save(newQuestion);

                return ChildResponseDTO.getAI.builder()
                        .ai(ai)
                        .isFinished(false).build();
        }
    }

    public ChildResponseDTO.getPresignedURLDTO getPresignedURL(Long childId, ChildRequestDTO.GetPresignedURLDTO request) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String contentType = request.getType().equals("video") ? "video/webm" : (request.getType().equals("image") ? "image/jpeg" : "");
        String extension = contentType.equals("video/webm") ? "webm" : (contentType.equals("image/jpeg") ? "jpeg" : "");
        String objectKey = childId + "/" + request.getSubjectId() + "/" + time + "." + extension;
        if(contentType.isEmpty() || extension.isEmpty()) throw new RuntimeException("contentType or objectKey is wrong : " + request.getType());

        String presignedURL = s3Service.generatePresignedUrl(objectKey, contentType, 600);
        return ChildResponseDTO.getPresignedURLDTO.builder()
                .url(presignedURL).build();
    }

    public void uploaded(ChildRequestDTO.UploadedDTO request) {
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Question> questions = questionRepository.findAllBySubject(subject);
        List<String> parts = Arrays.asList(request.getFile().split("\\."));
        String extension = parts.get(parts.size() - 1);

        switch(extension) {
            case "webm":
                for(Question question : questions) {
                    Analysis analysis = analysisRepository.findByQuestionId(question.getId()).orElseThrow(() -> new RuntimeException("Analysis not found"));
                    if(analysis.getVideo() == null) {
                        analysis.setVideo(request.getFile());
                        if(analysis.getImage() != null) analysis.setUploaded(true);

                        analysisRepository.save(analysis);
                        break;
                    }
                }
            case "jpeg":
                for(Question question : questions) {
                    Analysis analysis = analysisRepository.findByQuestionId(question.getId()).orElseThrow(() -> new RuntimeException("Analysis not found"));
                    if(analysis.getImage() == null) {
                        analysis.setImage(request.getFile());
                        if(analysis.getVideo() != null) analysis.setUploaded(true);

                        analysisRepository.save(analysis);
                        break;
                    }
                }
            default:
                throw new RuntimeException("Unsupported extension: " + extension);
        }
    }

    public ChildResponseDTO.finishedDTO answerFinished(Long childId, ChildRequestDTO.FinishedDTO request) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject not found"));


        //1. 대답 X, 주제 삭제
        //2. 기본 지정 질문은 이어서 가능하도록
        //2-2. 기본 지정 질문은 삭제 X
        //3. 추가 질문은 마무리

        return null;
    }

    //답변 완료 후, 알람 생성 시 호출
    private void makeNotice(Subject subject) {
        //답변완료 알람 생성
        Child child = subject.getChild();
        Family family = child.getFamily();
        List<Parent> parents = parentRepository.findAllByFamily(family);
        
        for(Parent parent : parents) {
            //답변 완료 알림
            Notice answerNotice = Notice.builder()
                    .child(child)
                    .subject(subject)
                    .type(1)
                    .receiver(parent)
                    .send_at(Timestamp.valueOf(LocalDateTime.now())).build();
            noticeRepository.save(answerNotice);
        
            //포도송이 생성 알림
            if(child.getGrape() % 6 == 0) {
                Notice grapeNotice = Notice.builder()
                        .child(child)
                        .receiver(parent)
                        .send_at(Timestamp.valueOf(LocalDateTime.now()))
                        .type(3).build();
                noticeRepository.save(grapeNotice);        
            }
        }
    }





    //이외 화면
    //public ChildResponseDTO.getRewardDTO getReward(Long childId) {}
    //public ChildResponseDTO.getStoreDTO getStore(Long childId) {}
    //public ChildResponseDTO.purchaseDTO purchase(Long childId, ChildRequestDTO.PurchaseDTO request) {}
}
