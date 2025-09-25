package com.ibridge.service;

import com.ibridge.domain.dto.request.ChildRequestDTO;
import com.ibridge.domain.dto.response.ChildResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final NoticeRepository noticeRepository;
    private final StoreRepository storeRepository;
    private final ChildStatRepository childStatRepository;
    private final ChildPositiveBoardRepository childPositiveBoardRepository;

    //질문 화면 관련
    public ChildResponseDTO.getQuestionDTO getHome(Long childId) {
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException(childId + "인 child가 없습니다."));
        boolean isCompleted = todaySubject.get(0).isCompleted();

        LocalDate today = LocalDate.now();
        ChildStat childStat = childStatRepository.findDateStatByChildandToday(child, today);
        boolean emotion = true;
        if(childStat.getEmotion() == null) {
            emotion = false;
        }

        return ChildResponseDTO.getQuestionDTO.builder()
                .grape(child.getGrape())
                .emotion(emotion)
                .isCompleted(isCompleted).build();
    }

    public void setEmotion(Long childId, ChildRequestDTO.setEmotionDTO request) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException(childId + "인 child가 없습니다."));
        LocalDate today = LocalDate.now();

        ChildStat dailyStat = childStatRepository.findDateStatByChildandToday(child, today);
        dailyStat.setEmotion(request.getEmotion());

        childStatRepository.save(dailyStat);

        return;
    }

    public ChildResponseDTO.getPredesignedQuestionDTO getPredesignedQuestion(Long childId) {
        LocalDate today = LocalDate.now();
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + "not Found"));
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, today);
        List<Question> questions = questionRepository.findAllBySubject(todaySubject.get(0));
        Question question = questions.get(questions.size() - 1);

        if(!todaySubject.get(0).isAnswer()) {
            ChildStat dateStat = childStatRepository.findDateStatByChildandToday(child, today);

            LocalDate monday = today.with(DayOfWeek.MONDAY);
            ChildStat weekStat = childStatRepository.findWeekStatByChildandToday(child, monday);

            String yearmonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-01";
            ChildStat monthStat = childStatRepository.findMonthStatByChildandToday(child, LocalDate.parse(yearmonth));

            long dateCount = dateStat.getAnswerCount();
            long weekCount = weekStat.getAnswerCount();
            long monthCount = monthStat.getAnswerCount();

            dateStat.setAnswerCount(dateCount + 1);
            weekStat.setAnswerCount(weekCount + 1);
            monthStat.setAnswerCount(monthCount + 1);

            childStatRepository.save(dateStat);
            childStatRepository.save(weekStat);
            childStatRepository.save(monthStat);
        }

        return ChildResponseDTO.getPredesignedQuestionDTO.builder()
                .question(question.getText())
                .subjectId(todaySubject.get(0).getId()).build();
    }

    public ChildResponseDTO.getNewQuestionDTO getNewSubject(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + "not Found"));
        LocalDate today = LocalDate.now();
        List<Subject> todaySubject = subjectRepository.findByChildIdAndDate(childId, LocalDate.now());
        Subject newSubject = Subject.builder()
                .child(childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found ")))
                .title("아이의 얘기 " + (todaySubject.size()))
                .date(LocalDate.now())
                .isAnswer(false)
                .isCompleted(false).build();
        subjectRepository.save(newSubject);

        ChildStat dateStat = childStatRepository.findDateStatByChildandToday(child, today);

        LocalDate monday = today.with(DayOfWeek.MONDAY);
        ChildStat weekStat = childStatRepository.findWeekStatByChildandToday(child, monday);

        LocalDate yearmonth = LocalDate.parse(today.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-01");
        ChildStat monthStat = childStatRepository.findMonthStatByChildandToday(child, yearmonth);

        long dateCount = dateStat.getAnswerCount();
        long weekCount = weekStat.getAnswerCount();
        long monthCount = monthStat.getAnswerCount();

        dateStat.setAnswerCount(dateCount + 1);
        weekStat.setAnswerCount(weekCount + 1);
        monthStat.setAnswerCount(monthCount + 1);

        childStatRepository.save(dateStat);
        childStatRepository.save(weekStat);
        childStatRepository.save(monthStat);

        Question question = Question.builder()
                .subject(newSubject)
                .text("무슨 이야기를 하고 싶어?").build();
        questionRepository.save(question);

        return ChildResponseDTO.getNewQuestionDTO.builder()
                .subjectId(newSubject.getId()).build();
    }

    public ChildResponseDTO.getAI getNextQuestion(Long childId, ChildRequestDTO.AnswerDTO request) {
        System.out.println("Requesting Next Question: " + request.getText());
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found "));
        LocalDate today = LocalDate.now();
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

            List<Long> qIds = questions.stream().map(Question::getId).toList();
            List<Analysis> analyses = analysisRepository.findAllByQuestionIdIn(qIds);
            Map<Long, String> answerByQid = analyses.stream()
                    .collect(Collectors.toMap(a -> a.getQuestion().getId(), Analysis::getAnswer));

            StringBuilder sb = new StringBuilder(1024);
            for (Question q : questions) {
                sb.append(q.getText()).append('\n')
                        .append(answerByQid.get(q.getId())).append('\n');
            }
            String conv = sb.toString();
            String summary = gptService.summarizeGPT(conv);
            targetSubject.setTitle(summary);
            subjectRepository.save(targetSubject);

//            String keyword = "temp";
//            int positive = 50;
//
//            String year = today.format(DateTimeFormatter.ofPattern("yyyy")) + "-01-01";
//            String yearmonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-01";
//            ChildPositiveBoard cbMonth = childPositiveBoardRepository.findByKeywordandChildwithDatewithType(keyword, child, yearmonth, PeriodType.MONTH).orElse(
//                    ChildPositiveBoard.builder()
//                            .child(child)
//                            .keyword(keyword)
//                            .period(yearmonth)
//                            .keywordCount(0L)
//                            .type(PeriodType.MONTH)
//                            .positive(0L).build()
//            );
//            ChildPositiveBoard cbYear = childPositiveBoardRepository.findByKeywordandChildwithDatewithType(keyword, child, year, PeriodType.YEAR).orElse(
//                    ChildPositiveBoard.builder()
//                            .child(child)
//                            .keyword(keyword)
//                            .period(year)
//                            .keywordCount(0L)
//                            .type(PeriodType.YEAR)
//                            .positive(0L).build()
//            );
//            ChildPositiveBoard cbAll = childPositiveBoardRepository.findByKeywordandChildwithDatewithType(keyword, child, "0000-00-00", PeriodType.CUMULATIVE).orElse(
//                    ChildPositiveBoard.builder()
//                            .child(child)
//                            .keyword(keyword)
//                            .period("0000-00-00")
//                            .keywordCount(0L)
//                            .type(PeriodType.CUMULATIVE)
//                            .positive(0L).build()
//            );
//
//            long newMonthPositive = Math.round((cbMonth.getPositive() * cbMonth.getKeywordCount() + positive) / (cbMonth.getKeywordCount() + 1));
//            long newYearPositive = Math.round((cbYear.getPositive() * cbYear.getKeywordCount() + positive) / (cbYear.getKeywordCount() + 1));
//            long newAllPositive = Math.round((cbAll.getPositive() * cbAll.getKeywordCount() + positive) / (cbAll.getKeywordCount() + 1));
//
//            cbMonth.setKeywordCount(cbMonth.getKeywordCount() + 1);
//            cbMonth.setPositive(newYearPositive);
//            cbYear.setKeywordCount(cbYear.getKeywordCount() + 1);
//            cbYear.setPositive(newMonthPositive);
//            cbAll.setKeywordCount(cbAll.getKeywordCount() + 1);
//            cbAll.setPositive(newAllPositive);
//
//            childPositiveBoardRepository.save(cbMonth);
//            childPositiveBoardRepository.save(cbYear);
//            childPositiveBoardRepository.save(cbAll);

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
        System.out.println("Requesting PresignedURL for Subject" + request.getSubjectId());
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

        String url = s3Service.generatePresignedUrl(objectKey, contentType, 600);
        System.out.println("Presigned URL: " + url);
        return ChildResponseDTO.getPresignedURLDTO.builder()
                .url(url).build();
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

    public ChildResponseDTO.finishedDTO answerFinished(Long childId, ChildRequestDTO.FinishedDTO request) {
        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child " + childId + " Not Found "));

        //포도송이 추가
        Long before = child.getGrape();
        ChildResponseDTO.finishedDTO data = ChildResponseDTO.finishedDTO.builder()
                    .grape(before + 1).build();
        child.setGrape(before + 1);
        childRepository.save(child);

        //군집화 진행
        int subjectCount = subjectRepository.countByChild(child);
        if(subjectCount % 15 == 0 && subjectCount != 0) {
            int clustering = subjectCount >= 60 ? 60 : subjectCount;
            //클러스터링 진행
            List<Subject> clusteringSubjectList = subjectRepository.findClusteringSubjectbyChild(child, clustering);
            Map<String, List<String>> categorized = gptService.categorizeSubjects(clusteringSubjectList.stream()
                    .map(Subject::getTitle) // Subject 객체에서 title만 뽑기
                    .collect(Collectors.toList()));

            for(Parent p : parentRepository.findAllByFamily(child.getFamily())) {
                Notice notice = Notice.builder()
                        .child(child)
                        .send_at(Timestamp.valueOf(LocalDateTime.now()))
                        .type(4)
                        .receiver(p)
                        .isRead(false)
                        .build();
                noticeRepository.save(notice);
            }
        }

        LocalDate today = LocalDate.now();
        String yearmonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-01";

        System.out.println("Requesting answer is finished in mid: " + request.getSubjectId());
        Subject predesigned = subjectRepository.findByChildIdAndDate(childId, LocalDate.now()).get(0);

        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow(() -> new RuntimeException("Subject " + request.getSubjectId() + " Not Found "));
        List<Question> questions = questionRepository.findAllBySubject(subject);

        if(predesigned == subject) {    //기본 지정 질문 완료
            subject.setAnswer(true);
            subjectRepository.save(subject);
        }
        else if(questions.size() == 1) {   //대답 X, 주제 삭제
            System.out.println("questions' size == 1, deleted");
            questionRepository.delete(questions.get(0));
            subjectRepository.delete(subject);
        }
        else if(subject.isCompleted()) {   //이미 Complete된 상태
            System.out.println("subject is completed");
        }
        else {   //나머지
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

        return data;
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
            noticeRepository.save(p);

            if(child.getGrape() % 6 == 0) {
                Notice completeNotice = Notice.builder()
                        .child(child)
                        .send_at(Timestamp.valueOf(LocalDateTime.now()))
                        .type(3)
                        .receiver(parent)
                        .isRead(false)
                        .subject(subject)
                        .build();
                noticeRepository.save(completeNotice);
            }
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