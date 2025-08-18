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
import java.util.Arrays;
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
    private final StoreRepository storeRepository;
    private final ChildStatRepository childStatRepository;

    //질문 화면 관련
    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException(childId + "인 child가 없습니다."));
        boolean isCompleted = todaySubject.get(0).isCompleted();

        ChildStat childStat = childStatRepository.findByChildToday(child, LocalDate.now().toString()).orElse(null);
        boolean emotion = true;
        if(childStat == null) {
            emotion = false;
        }

        return ChildResponseDTO.getQuestionDTO.builder()
                .emotion(emotion)
                .isCompleted(isCompleted).build();
    }

    public void setEmotion(Long childId, ChildRequestDTO.setEmotionDTO request) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException(childId + "인 child가 없습니다."));

        ChildStat newStat = ChildStat.builder()
                .child(child)
                .type()
                .period(LocalDate.now().toString())
                .emotion(Emotion.)
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
        System.out.println("Uploaded: " + request.getFile());
        List<Question> questions = questionRepository.findAllBySubject(subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject " + request.getSubjectId() + " Not Found ")));
        List<String> parts = Arrays.asList(request.getFile().split("\\."));
        String extension = parts.get(parts.size() - 1);

        switch(extension) {
            case "webm":
                for(Question question: questions) {
                    Analysis analysis = analysisRepository.findByQuestionId(question.getId()).orElseThrow(() -> new RuntimeException("Question " + question.getId() + " Not Found with Uploading " + request.getFile()));
                    if(analysis.getVideo() == null) {
                        analysis.setVideo(request.getFile());
                        analysisRepository.save(analysis);
                        break;
                    }
                }
                break;

            case "jpeg":
                for(Question question: questions) {
                    Analysis analysis = analysisRepository.findByQuestionId(question.getId()).orElseThrow(() -> new RuntimeException("Question " + question.getId() + " Not Found "));
                    if(analysis.getImage() == null) {
                        analysis.setImage(request.getFile());
                        analysisRepository.save(analysis);
                        break;
                    }
                }
                break;
            default:
                throw new RuntimeException("Unsupported extension: " + extension);
        }
    }

    public void answerFinished(Long childId, ChildRequestDTO.FinishedDTO request) {
        System.out.println("Requesting answer is finished in mid: " + request.getSubjectId());
        Subject predesigned = subjectRepository.findByChildIdAndDate(childId, LocalDate.now()).get(0);

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject " + request.getSubjectId() + " Not Found "));
        List<Question> questions = questionRepository.findAllBySubject(subject);

        if(predesigned == subject) {
            subject.setAnswer(true);
            subjectRepository.save(subject);
        }
        else if(questions.size() == 1) {
            System.out.println("questions' size == 1, deleted");
            questionRepository.delete(questions.get(0));
            subjectRepository.delete(subject);
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



    //이외 화면
    public ChildResponseDTO.getRewardDTO getReward(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found"));
        return ChildResponseDTO.getRewardDTO.builder()
                .grape(child.getGrape())
                .build();
    }

    public ChildResponseDTO.getStoreDTO getStore(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found"));
        List<Store> Items = storeRepository.findAll();
        List<ChildResponseDTO.getStoreItemsDTO> items = new ArrayList<>();
        for(Store store: Items) {
            ChildResponseDTO.getStoreItemsDTO temp = ChildResponseDTO.getStoreItemsDTO.builder()
                    .id(store.getId())
                    .name(store.getName())
                    .cost(store.getCost()).build();
            items.add(temp);
        }

        return ChildResponseDTO.getStoreDTO.builder()
                .grape(child.getGrape())
                .items(items).build();
    }

    public ChildResponseDTO.purchaseDTO purchase(Long childId, ChildRequestDTO.PurchaseDTO request) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found"));
        Store item = storeRepository.findById(request.getItemId()).orElseThrow(() -> new RuntimeException("Store " + request.getItemId() + " Not Found"));

        Long cur = child.getGrape();
        Long cost = item.getCost() * 6;
        if(cost > cur) {
            throw new RuntimeException("포도송이가 부족합니다");
        }

        Long newGrapes = cur - cost;
        child.setGrape(newGrapes);
        childRepository.save(child);

        return ChildResponseDTO.purchaseDTO.builder()
                .grape(newGrapes).build();
    }
}