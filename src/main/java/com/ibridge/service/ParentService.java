package com.ibridge.service;

import com.ibridge.domain.dto.request.NoticeRequestDTO;
import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;
    private final FamilyRepository famliyRepository;
    private final NoticeRepository NoticeRepository;
    private final Random random = new Random();
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final AnalysisRepository analysisRepository;
    private final NoticeRepository noticeRepository;
    private final FamilyRepository familyRepository;
    private final ChildStatRepository childStatRepository;
    private final ChildPositiveBoardRepository childPositiveBoardRepository;

    public ParentHomeResponseDTO getParentHome(Long childId, LocalDate date, String email) {
        Child child = childRepository.findById(childId).orElse(null);
        List<SubjectDTO> subjects = subjectRepository.findByChildIdAndDate(childId, date).stream()
                .map(subject -> new SubjectDTO(subject.getId(), subject.getTitle(), subject.isAnswer()))
                .collect(Collectors.toList());

        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        ChildStat cs = childStatRepository.findByType(child).orElse(null);
        ChildPositiveBoard cpb = childPositiveBoardRepository.findTopByChild(child);

        List<ChildPositiveBoard> boards = childPositiveBoardRepository.findByChildAndPeriodBetween(child, startDate, endDate);
        String highestPositiveCategory = null;
        String highestNegativeCategory = null;

        if (!boards.isEmpty()) {
            ChildPositiveBoard maxPositiveBoard = boards.stream()
                    .max(Comparator.comparingDouble(b -> (double) b.getPositive() / b.getKeywordCount()))
                    .orElse(null);
            highestPositiveCategory = maxPositiveBoard != null ? maxPositiveBoard.getKeyword() : null;

            ChildPositiveBoard maxNegativeBoard = boards.stream()
                    .max(Comparator.comparingDouble(b -> 1.0 - (double) b.getPositive() / b.getKeywordCount()))
                    .orElse(null);
            highestNegativeCategory = maxNegativeBoard != null ? maxNegativeBoard.getKeyword() : null;
        }
        ChildStat csToday = childStatRepository.findByChildAndPeriod(child, LocalDate.now());
        List<ChildStat> stats = childStatRepository.findByChildAndPeriodBetween(child, startDate, endDate);
        Integer mostSelectedEmotion = stats.stream()
                .collect(Collectors.groupingBy(ChildStat::getEmotion, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        Long grape = cs.getAnswerCount()/6-(cs.getAnswerCount()- csToday.getAnswerCount())/6;

        return ParentHomeResponseDTO.builder()
                .newGrape(Integer.parseInt(grape+""))
                .name(child.getName())
                .cumulativeAnswerCount(Integer.parseInt(cs.getAnswerCount()+""))
                .mostTalkedCategory(cpb.getKeyword())
                .positiveCategory(highestPositiveCategory)
                .negativeCategory(highestNegativeCategory)
                .emotion(mostSelectedEmotion)
                .subjects(subjects)
                .build();
    }

    public readSubjectsResponseDTO readSubjects(Parent parent, Long childId, Long year, Long month) {
        boolean[] arr = new boolean[32];
        Child child = childRepository.findById(childId).orElse(null);
        List<Notice> notices = noticeRepository.findAllByReceiverAndChild(parent, year, month, child);
        for(Notice notice : notices) {
            Timestamp sendAt = notice.getSend_at();
            LocalDateTime localDateTime = sendAt.toLocalDateTime();
            int day = localDateTime.getDayOfMonth();
            arr[day] = true;
        }
        List<Boolean> result = new ArrayList<>();
        for(int i = 1;i<32;i++) {
            result.add(arr[i]);
        }

        return readSubjectsResponseDTO.builder()
                .month(result)
                .build();
    }
    public void openNotice(NoticeRequestDTO noticeRequestDTO) {
        noticeRepository.deleteById(noticeRequestDTO.getNoticeId());
    }

    public AnalysisResponseDTO getDefaultAnalysis(Long childId) {
        Child child = childRepository.findById(childId) .orElseThrow(() -> new RuntimeException("Child not found"));
        YearMonth yearMonth = YearMonth.from(LocalDate.now());
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        //누적 응담 수
        ChildStat cs = childStatRepository.findByType(child).orElseThrow(() -> new RuntimeException("ChildStat not found"));
        // 감정 목록 조회, null이면 0으로 채움 (달의 일 수만큼)
        List<ChildStat> emotionsFromDb = childStatRepository.findEmotionsByChildAndMonth(child, start, end);
        int daysInMonth = yearMonth.lengthOfMonth();
        Integer[] emotions = new Integer[daysInMonth+1];
        Arrays.fill(emotions, 0);
        for(ChildStat childStat : emotionsFromDb) {
            if(childStat.getEmotion() != null){
                emotions[childStat.getPeriod().getDayOfMonth()] = childStat.getEmotion();
            }
        }
        // 최근 7일 기간 리스트
        List<LocalDate> periodList = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            periodList.add(LocalDate.now().minusDays(i));
        }

        // cumList 조회, null이면 0으로 채움
        List<Long> cumFromDb = childStatRepository.findAnswerCountsByChildAndPeriodList(child, periodList);
        List<Long> cumList = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            if (cumFromDb != null && i < cumFromDb.size() && cumFromDb.get(i) != null) {
                cumList.add(cumFromDb.get(i)); }
            else {
                cumList.add(0L);
            }
        }
        List<KeywordDTO> keywordDTOs = childPositiveBoardRepository.findkeywordsByChild(childId);

        return AnalysisResponseDTO.builder()
                .name(child.getName())
                .signupDate(cs.getPeriod())
                .cumulative(cs.getAnswerCount())
                .keywords(keywordDTOs)
                .emotions(Arrays.asList(emotions))
                .cumList(cumList)
                .build();
    }

    public AnalysisResponseDTO getEmotions(Long childId, LocalDate today) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        YearMonth yearMonth = YearMonth.from(today);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<ChildStat> emotionsFromDb = childStatRepository.findEmotionsByChildAndMonth(child, start, end);
        int daysInMonth = yearMonth.lengthOfMonth();
        Integer[] emotions = new Integer[daysInMonth+1];
        Arrays.fill(emotions, 0);
        for(ChildStat childStat : emotionsFromDb) {
            if(childStat.getEmotion() != null){
                emotions[childStat.getPeriod().getDayOfMonth()] = childStat.getEmotion();
            }
        }

        return AnalysisResponseDTO.builder()
                .emotions(Arrays.asList(emotions))
                .build();
    }





    public AnalysisResponseDTO getCumulatives(Long childId, String periodType) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        YearMonth yearMonth = YearMonth.from(LocalDate.now());
        LocalDate start = yearMonth.atDay(1);
        List<Long> cumulatives = new ArrayList<>();

        switch (periodType) {
            case "day":
                for (int i = 6; i >= 0; i--) {
                    LocalDate targetDate = LocalDate.now().minusDays(i);
                    ChildStat stat = childStatRepository.findDateStatByChildandToday(child, targetDate);
                    cumulatives.add(stat != null && stat.getAnswerCount() != null ? stat.getAnswerCount() : 0L);
                }
                break;

            case "week":
                for (int i = 6; i >= 0; i--) {
                    LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(i);
                    ChildStat stat = childStatRepository.findWeekStatByChildandToday(child, monday);
                    cumulatives.add(stat != null && stat.getAnswerCount() != null ? stat.getAnswerCount() : 0L);
                }
                break;

            case "month":
                for (int i = 6; i >= 0; i--) {
                    LocalDate firstDayOfMonth = start.minusMonths(i);
                    ChildStat stat = childStatRepository.findMonthStatByChildandToday(child, firstDayOfMonth);
                    cumulatives.add(stat != null && stat.getAnswerCount() != null ? stat.getAnswerCount() : 0L);
                }
                break;
        }
        return AnalysisResponseDTO.builder()
                .cumList(cumulatives)
                .build();
    }

    public CategorySubjectDTO getSubjects(Long childId, String keyword) {
        Optional<Child> child = childRepository.findById(childId);
        Pageable top60 = PageRequest.of(0,60);
        List<SubjectDTO> subjects = subjectRepository.findRecentSubjectDTOs(child.get(), keyword, top60);

        return CategorySubjectDTO.builder()
                .subjects(subjects)
                .build();
    }

//    public AnalysisResponseDTO getKeywords(Long childId, String periodType, String periodValue) {
//        Child child = childRepository.findById(childId).orElseThrow(() -> new RuntimeException("Child not found"));
//        PageRequest top7 = PageRequest.of(0,7);
//        PeriodType type = PeriodType.valueOf(periodType);
//        List<ChildPositiveBoard> childPositiveBoards = childPositiveBoardRepository.findByChildAndTypeAndPeriod(child, type, periodValue, top7);
//        List<KeywordDTO> keywords = new ArrayList<>();
//        for(ChildPositiveBoard childPositiveBoard : childPositiveBoards){
//            keywords.add(KeywordDTO.builder()
//                            .keyword(childPositiveBoard.getKeyword())
//                            .count(childPositiveBoard.getKeywordCount())
//                            .positiveScore(childPositiveBoard.getPositive())
//                            .build());
//        }
//        return AnalysisResponseDTO.builder()
//                .keywords(keywords)
//                .build();
//    }






//현호
    public ParentResponseDTO.GetMyPageDTO getMyPage(Long parentId) {
    Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not Found"));
    List<Notice> Notice = NoticeRepository.findAllByParent(parent);
    boolean noticeExist = false;
    for(Notice notice : Notice) {
        if(!notice.isRead()){
            noticeExist = true;
        }
    }

    Family family = parent.getFamily();
    List<ParentResponseDTO.ChildSimpleInfoDTO> childSimpleInfoDTOListDTOList = new ArrayList<>();
    List<Child> children = childRepository.findAllByFamily(family);
    for(Child child : children) {
        ParentResponseDTO.ChildSimpleInfoDTO childDTO = ParentResponseDTO.ChildSimpleInfoDTO.builder()
                .childId(child.getId())
                .childName(child.getName()).build();
        childSimpleInfoDTOListDTOList.add(childDTO);
    }

    return ParentResponseDTO.GetMyPageDTO.builder()
            .noticeExist(noticeExist)
            .name(parent.getName())
            .familyName(family.getName())
            .children(childSimpleInfoDTOListDTOList).build();
    }

    public ParentResponseDTO.GetFamilyInfoDTO getFamilyPage(Long parentId) {
        Parent parent = parentRepository.findById(parentId).get();
        Family family = parent.getFamily();

        List<ParentResponseDTO.ParentInfoDTO> parentInfoDTOList = new ArrayList<>();
        for(Parent otherParent : parentRepository.findAllByFamily(family)) {
            parentInfoDTOList.add(ParentResponseDTO.ParentInfoDTO.builder()
                    .parentId(otherParent.getId())
                    .parentName(otherParent.getName()).build());
        }

        List<ParentResponseDTO.ChildInfoDTO> childInfoDTOList = new ArrayList<>();
        for(Child child : childRepository.findAllByFamily(family)) {
            childInfoDTOList.add(ParentResponseDTO.ChildInfoDTO.builder()
                    .childId(child.getId())
                    .childName(child.getName())
                    .childBirth(child.getBirth().toString())
                    .childGender(child.getGender().ordinal()).build());
        }

        return ParentResponseDTO.GetFamilyInfoDTO.builder()
                .familyName(family.getName())
                .parents(parentInfoDTOList)
                .children(childInfoDTOList).build();
    }

    public void editFamilyName(Long parentId, ParentRequestDTO.editFamilyNameDTO request) {
        Parent parent = parentRepository.findById(parentId).get();
        Family family = parent.getFamily();

        List<Family> otherFamily = familyRepository.findAll();
        for(Family other : otherFamily) {
            if(other.getName().equals(request.getFamilyName())) throw new RuntimeException("This name is duplicated");
        }

        family.setName(request.getFamilyName());
    }

    public void deleteAccount(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        if(parentRepository.getParentCount(family) > 1) {
            parentRepository.deleteById(parentId);
            return;
        }

        for(Child child : childRepository.findAllByFamily(family)) {
            childRepository.delete(child);
        }
        famliyRepository.delete(family);
    }

    public ParentResponseDTO.ChildIdDTO addChild(Long parentId, ParentRequestDTO.AddChildDTO request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        Child child = Child.builder()
                .family(family)
                .name(request.getName())
                .birth(LocalDate.parse(request.getBirthday()))
                .gender(Gender.values()[request.getGender()]).build();

        //주제 및 질문 설정
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

        for (int i = 0; i < 14; i++) {
            LocalDate targetDate = LocalDate.now().plusDays(i);

            int id = random.nextInt(subjects.size());
            String randomQuestion = subjects.get(id);

            Subject subject = Subject.builder()
                    .title(randomQuestion)
                    .child(child)
                    .date(targetDate)
                    .isAnswer(false)
                    .build();
            subjectRepository.save(subject);

            Question question = Question.builder()
                    .subject(subject)
                    .text(randomQuestion)
                    .build();
            questionRepository.save(question);
        }

        //ChildStat 설정
        ChildStat dailyStat = ChildStat.builder()
                .child(child)
                .period(LocalDate.now())
                .answerCount(0L)
                .type(PeriodType.DAY)
                .emotion(null).build();
        ChildStat weeklyStat = ChildStat.builder()
                .child(child)
                .period(LocalDate.now().with(DayOfWeek.MONDAY))
                .answerCount(0L)
                .type(PeriodType.WEEK)
                .emotion(null).build();
        ChildStat monthlyStat = ChildStat.builder()
                .child(child)
                .period(LocalDate.now().withDayOfMonth(1))
                .type(PeriodType.MONTH)
                .emotion(null).build();
        ChildStat totalStat = ChildStat.builder()
                .child(child)
                .period(LocalDate.now())
                .type(PeriodType.CUMULATIVE)
                .emotion(null).build();

        childStatRepository.save(dailyStat);
        childStatRepository.save(weeklyStat);
        childStatRepository.save(monthlyStat);
        childStatRepository.save(totalStat);

        return ParentResponseDTO.ChildIdDTO.builder()
                .childId(childRepository.save(child).getId()).build();
    }

    public ParentResponseDTO.ChildIdDTO patchChild(ParentRequestDTO.EditChildDTO request) throws ParseException {
        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));
        child.setName(request.getName());
        child.setBirth(LocalDate.parse(request.getBirthday()));
        child.setGender(Gender.values()[request.getGender()]);
        childRepository.save(child);

        return ParentResponseDTO.ChildIdDTO.builder()
                .childId(child.getId()).build();
    }

    public void deleteChild(ParentRequestDTO.DeleteChildDTO request) {
        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));
        List<Subject> subjectList = subjectRepository.findAllByChild(child);
        for(Subject subject : subjectList) {
            List<Question> questions = questionRepository.findAllBySubject(subject);
            for(Question question : questions) {
                Optional<Analysis> analysis = analysisRepository.findByQuestionId(question.getId());
                if(analysis.isPresent()) analysisRepository.delete(analysis.get());
                questionRepository.delete(question);
            }
            subjectRepository.delete(subject);
        }

        List<ChildStat> childStatList = childStatRepository.findAllByChild(child);
        childStatRepository.deleteAll(childStatList);
        childRepository.delete(child);
    }



    //알림
    public ParentResponseDTO.NoticeCheckDTO getNotice(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        List<ParentResponseDTO.NoticeDTO> noticeDTOList = new ArrayList<>();
        List<Notice> noticesForParent = NoticeRepository.findAllByParent(parent);

        for(Notice notice : noticesForParent) {
            notice.setRead(true);
            NoticeRepository.save(notice);

            noticeDTOList.add(ParentResponseDTO.NoticeDTO.builder()
                    .noticeId(notice.getId())
                    .senderId(notice.getChild() == null? notice.getSender().getId() : notice.getChild().getId())
                    .type(notice.getType())
                    .time(notice.getSend_at().toString())
                    .subject(notice.getSubject() == null? null : notice.getSubject().getId())
                    .isAccept(notice.isAccept())
                    .senderName(notice.getChild() == null? notice.getSender().getName() : notice.getChild().getName()).build()
            );
        }

        return ParentResponseDTO.NoticeCheckDTO.builder().notices(noticeDTOList).build();
    }

    public void addParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();
        Parent requester = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));

        Notice requested = NoticeRepository.findByReceiverandSendertoJoinFamily(parent, requester);
        requested.setAccept(true);
        NoticeRepository.save(requested);
        NoticeRepository.delete(requested);
        List<Notice> otherRequested = NoticeRepository.findAllReceiverBySender(requester);
        for(Notice otherRequest : otherRequested) {
            otherRequest.setAccept(true);
            NoticeRepository.save(otherRequest);
            NoticeRepository.delete(otherRequest);
        }

        requester.setFamily(family);
        parentRepository.save(requester);
    }

    public void declineParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Parent sender = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));

        Notice requested = NoticeRepository.findByReceiverandSendertoJoinFamily(parent, sender);
        NoticeRepository.delete(requested);
    }


}
