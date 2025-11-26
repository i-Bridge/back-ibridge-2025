package com.ibridge.service;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Not;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private final Random random = new Random();
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final AnalysisRepository analysisRepository;
    private final NoticeRepository noticeRepository;
    private final FamilyRepository familyRepository;
    private final ChildStatRepository childStatRepository;
    private final ChildPositiveBoardRepository childPositiveBoardRepository;

    public ParentHomeResponseDTO getParentHome(Long childId, Pageable pageable, String email) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("해당 자녀를 찾을 수 없습니다: " + childId));

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "date")
        );

        // 요청한 페이지 사이즈보다 하나 더 가져와서 hasNext 계산
        int pageSize = sortedPageable.getPageSize();
        int pageNumber = sortedPageable.getPageNumber();

        List<Subject> subjectEntities = subjectRepository.findByChildIdAndIsCompleted(
                childId,
                true,
                sortedPageable.getSort()
        );

        // offset, limit 흉내 (메모리에서 처리)
        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize + 1, subjectEntities.size());

        List<Subject> pagedSubjects = subjectEntities.subList(fromIndex, toIndex);

        // hasNext 계산 → pageSize+1개를 가져왔다면 다음 페이지 있음
        boolean hasNext = pagedSubjects.size() > pageSize;

        // DTO 변환 (pageSize까지만 잘라서 반환)
        List<SubjectDTO> subjects = pagedSubjects.stream()
                .limit(pageSize)
                .map(subject -> {
                    String image = subject.getQuestions().stream()
                            .map(Question::getAnalysis)
                            .filter(Objects::nonNull)
                            .map(Analysis::getImage)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);

                    return new SubjectDTO(
                            subject.getId(),
                            subject.getTitle(),
                            subject.isAnswer(),
                            subject.getDate(),
                            image
                    );
                })
                .toList();

        return ParentHomeResponseDTO.builder()
                .subjects(subjects) // List<SubjectDTO>
                .hasNext(hasNext)   // 다음 페이지 여부
                .build();
    }


    /*public readSubjectsResponseDTO readSubjects(Parent parent, Long childId, Long year, Long month) {
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
    }*/

    public BannerDTO getBanner(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("해당하는 자녀를 찾을 수 없습니다. " + childId));

        // 누적 ChildStat
        ChildStat cs = childStatRepository.findByType(child).orElse(null);
        // 오늘 ChildStat
        ChildStat csToday = childStatRepository.findByChildAndPeriod(child, LocalDate.now());
        // 최신 ChildPositiveBoard
        ChildPositiveBoard latestBoard = childPositiveBoardRepository.findTopByChildOrderByPeriodDesc(child);

        // 자녀의 전체 ChildPositiveBoard 데이터 조회
        List<ChildPositiveBoard> boards = childPositiveBoardRepository.findByChild(child);

        String highestPositiveCategory = null;
        String highestNegativeCategory = null;

        if (!boards.isEmpty()) {
            // 긍정 비율 가장 높은 카테고리
            ChildPositiveBoard maxPositiveBoard = boards.stream()
                    .filter(b -> b.getKeyword() != null && b.getKeywordCount() > 0)
                    .max(Comparator.comparingDouble(b ->
                            (double) b.getPositive() / b.getKeywordCount()))
                    .orElse(null);

            highestPositiveCategory = (maxPositiveBoard != null)
                    ? maxPositiveBoard.getKeyword()
                    : null;

            // 부정 비율 가장 높은 카테고리
            ChildPositiveBoard maxNegativeBoard = boards.stream()
                    .filter(b -> b.getKeyword() != null && b.getKeywordCount() > 0)
                    .max(Comparator.comparingDouble(b ->
                            1.0 - (double) b.getPositive() / b.getKeywordCount()))
                    .orElse(null);

            highestNegativeCategory = (maxNegativeBoard != null)
                    ? maxNegativeBoard.getKeyword()
                    : null;
        }

        // 가장 최근에 대화한 카테고리 (optional)
        ChildPositiveBoard cpb = childPositiveBoardRepository.findTopByChild(child);
        String mostTalkedCategory = (cpb != null ? cpb.getKeyword() : null);

        return BannerDTO.builder()
                .date(latestBoard != null ? latestBoard.getPeriod() : null)
                .cumulativeAnswerCount(cs != null ? cs.getAnswerCount() : 0)
                .mostTalkedCategory(mostTalkedCategory)
                .positiveCategory(highestPositiveCategory)
                .negativeCategory(highestNegativeCategory)
                .name(child.getName())
                .build();
    }


    public ParentHomeResponseDTO openNotice(Long childId, Long subjectId, Long noticeId) {
        noticeRepository.deleteById(noticeId);

        // DTO 직접 조회
        List<SubjectDTO> subjects = subjectRepository.findSubjectDTOsByChildId(childId);

        int idx = -1;
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).getSubjectId().equals(subjectId)) {
                idx = i;
                break;
            }
        }

        if (idx == -1) {
            throw new IllegalArgumentException("해당 subjectId를 찾을 수 없습니다. (isComplete=true 조건 확인)");
        }

        // 앞 4개, 뒤 5개 (총 10개)
        int fromIndex = Math.max(0, idx - 4);
        int toIndex = Math.min(subjects.size(), idx + 6);

        List<SubjectDTO> subjectDTOs = subjects.subList(fromIndex, toIndex);

        int pageSize = 10;
        int page = idx / pageSize + 1;

        boolean hasNext = toIndex < subjects.size();

        return ParentHomeResponseDTO.builder()
                .subjects(subjectDTOs)
                .page(page)
                .hasNext(hasNext)
                .build();
    }


    public AnalysisResponseDTO getDefaultAnalysis(Long childId) {
        Child child = childRepository.findById(childId) .orElseThrow(() -> new RuntimeException("Child not found"));
        YearMonth yearMonth = YearMonth.from(LocalDate.now());
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth(); //누적 응담 수
        ChildStat cs = childStatRepository.findByType(child).orElseThrow(() -> new RuntimeException("ChildStat not found"));
        // 감정 목록 조회, null이면 0으로 채움 (달의 일 수만큼)
        List<ChildStat> emotionsFromDb = childStatRepository.findEmotionsByChildAndMonth(child, start, end);
        int daysInMonth = yearMonth.lengthOfMonth();
        Integer[] emotions = new Integer[daysInMonth+1]; Arrays.fill(emotions, 0);
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
                cumList.add(cumFromDb.get(i));
            }
            else {
                cumList.add(0L);
            }
        }
        List<KeywordDTO> keywordDTOs = childPositiveBoardRepository.findKeywordsByChildId(childId);
        return AnalysisResponseDTO.builder()
                .categories(keywordDTOs)
                .build();
    }

    public EmotionAnalysisResponseDTO getEmotions(Long childId, LocalDate date) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        // 2) 요청일 기준 YearMonth 범위
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        // 3) 이번 달의 ChildStat(감정) 조회 (period 오름차순)
        List<ChildStat> stats = childStatRepository.findEmotionsByChildAndMonth(child, start, end);

        // 4) 날짜별 감정 배열 초기화
        // 배열 길이를 daysInMonth+1로 하여 dayOfMonth 인덱스를 그대로 사용(0 인덱스는 unused)
        Integer[] emotions = new Integer[daysInMonth + 1];
        Arrays.fill(emotions, 0); // 기본값 0

        for (ChildStat cs : stats) {
            if (cs.getEmotion() != null && cs.getPeriod() != null) {
                int day = cs.getPeriod().getDayOfMonth();
                if (day >= 1 && day <= daysInMonth) {
                    emotions[day] = cs.getEmotion();
                }
            }
        }

        // 5) 이번 달 최빈 감정 계산 (0은 무시)
        int mostFrequentEmotion =
                java.util.stream.IntStream.rangeClosed(1, daysInMonth)
                        .map(i -> emotions[i] == null ? 0 : emotions[i])
                        .filter(v -> v != 0)
                        .boxed()
                        .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                        .entrySet().stream()
                        .max(Map.Entry.<Integer, Long>comparingByValue()
                                .thenComparing(Map.Entry.comparingByKey()))
                        .map(Map.Entry::getKey)
                        .orElse(0);

        // 6) signupDate 결정: 가능한 경우 가장 오래된 ChildStat.period 사용
        Optional<ChildStat> firstStatOpt = childStatRepository.findFirstByChildOrderByPeriodAsc(child);
        LocalDate signupDate = firstStatOpt.map(ChildStat::getPeriod).orElse(null);

        // 7) AnalysisResponseDTO 구성
        EmotionAnalysisResponseDTO dto = EmotionAnalysisResponseDTO.builder()
                .signupDate(signupDate)
                .emotion(mostFrequentEmotion)
                .emotions(Arrays.asList(emotions)) // 인덱스 0은 unused(0)
                .build();

        return dto;
    }





    public AnalysisResponseDTO getCumulatives(Long childId, String periodType) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        YearMonth yearMonth = YearMonth.from(LocalDate.now());
        LocalDate start = yearMonth.atDay(1);
        List<Long> cumulatives = new ArrayList<>();
        Optional<ChildStat> cs = childStatRepository.findByType(child);
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
                .cumulative(cs.get().getAnswerCount())
                .cumList(cumulatives)
                .build();
    }

    public CategorySubjectDTO getSubjects(Long childId, String keyword) {
        Optional<Child> child = childRepository.findById(childId);
        Pageable top60 = PageRequest.of(0, 60);

        // keyword 디코딩
        String decodedKeyword = null;
        if (keyword != null && !keyword.isEmpty()) {
            decodedKeyword = URLDecoder.decode(keyword, StandardCharsets.UTF_8);
        }

        List<SubjectDTO> subjects = subjectRepository.findRecentSubjectDTOs(child.get(), decodedKeyword, top60);

        return CategorySubjectDTO.builder()
                .subjects(subjects)
                .build();
    }

    public SubjectListResponseDTO getSubjectsByDate(Long childId, LocalDate date) {
        if (date == null) date = LocalDate.now();

        List<Subject> subjects = subjectRepository.findAllByChildIdAndDateWithQuestions(childId, date);

        List<SubjectListResponseDTO.SubjectResponseDTO> subjectDTOs = subjects.stream()
                .map(subject -> SubjectListResponseDTO.SubjectResponseDTO.builder()
                        .subjectId(subject.getId())
                        .title(subject.getTitle())
                        .questions(subject.getQuestions().stream()
                                .map(q -> {
                                    Analysis a = q.getAnalysis();
                                    return SubjectListResponseDTO.QuestionResponseDTO.builder()
                                            .text(q.getText())
                                            .answer(a != null ? a.getAnswer() : null)
                                            .image(a != null ? a.getImage() : null)
                                            .video(a != null ? a.getVideo() : null)
                                            .build();
                                })
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return SubjectListResponseDTO.builder()
                .subjectCount(subjectDTOs.size())
                .subjects(subjectDTOs)
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
    List<Notice> Notice = noticeRepository.findAllByParent(parent);
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
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent " + parentId + "Not Found"));
        Family family = parent.getFamily();

        List<ParentResponseDTO.ParentInfoDTO> parentInfoDTOList = new ArrayList<>();
        for(Parent otherParent : parentRepository.findAllByFamily(family)) {
            parentInfoDTOList.add(ParentResponseDTO.ParentInfoDTO.builder()
                    .id(otherParent.getId())
                    .email(otherParent.getEmail())
                    .name(otherParent.getName())
                    .own(otherParent.equals(parent)).build());
        }

        List<ParentResponseDTO.ChildInfoDTO> childInfoDTOList = new ArrayList<>();
        for(Child child : childRepository.findAllByFamily(family)) {
            childInfoDTOList.add(ParentResponseDTO.ChildInfoDTO.builder()
                    .id(child.getId())
                    .name(child.getName())
                    .birth(child.getBirth().toString())
                    .gender(child.getGender().toString()).build());
        }

        return ParentResponseDTO.GetFamilyInfoDTO.builder()
                .familyName(family.getName())
                .parents(parentInfoDTOList)
                .children(childInfoDTOList)
                .parentCount(parentInfoDTOList.size())
                .childCount(childInfoDTOList.size()).build();
    }

    public void editName(Parent parent, ParentRequestDTO.editNameDTO request) {
        parent.setName(request.getName());
        parentRepository.save(parent);
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
                .gender(Gender.values()[request.getGender()])
                .grape(0L).build();
        Long childId = childRepository.save(child).getId();



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
                .answerCount(0L)
                .type(PeriodType.MONTH)
                .emotion(null).build();
        ChildStat totalStat = ChildStat.builder()
                .child(child)
                .answerCount(0L)
                .period(LocalDate.now())
                .type(PeriodType.CUMULATIVE)
                .emotion(null).build();

        childStatRepository.save(dailyStat);
        childStatRepository.save(weeklyStat);
        childStatRepository.save(monthlyStat);
        childStatRepository.save(totalStat);

        return ParentResponseDTO.ChildIdDTO.builder()
                .childId(childId).build();
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
        Family family = child.getFamily();
        List<Notice> noticeList = noticeRepository.findAllByChild(child);
        noticeRepository.deleteAll(noticeList);

        List<Child> childCount = childRepository.findAllByFamily(family);
        if(childCount.isEmpty()) throw new RuntimeException("자식 수가 1개라 삭제가 불가능합니다.");

        List<Subject> subjectList = subjectRepository.findAllByChild(child);
        for(Subject subject : subjectList) {
            List<Question> questions = questionRepository.findAllBySubject(subject);
            for(Question question : questions) {
                Optional<Analysis> analysis = analysisRepository.findByQuestionId(question.getId());
                analysis.ifPresent(analysisRepository::delete);
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
        List<Notice> noticesForParent = noticeRepository.findAllByParent(parent);

        int count = 0;
        for(Notice notice : noticesForParent) {
            notice.setRead(true);
            noticeRepository.save(notice);

            noticeDTOList.add(ParentResponseDTO.NoticeDTO.builder()
                    .noticeId(notice.getId())
                    .senderId(notice.getChild() == null? notice.getSender().getId() : notice.getChild().getId())
                    .type(notice.getType())
                    .time(notice.getSend_at().toString())
                    .subject(notice.getSubject() == null? null : notice.getSubject().getId())
                    .isAccept(notice.isAccept())
                    .senderName(notice.getChild() == null? notice.getSender().getName() : notice.getChild().getName())
                    .isRead(notice.isRead()).build()
            );
            if(!notice.isRead()) count++;
        }

        return ParentResponseDTO.NoticeCheckDTO.builder()
                .notices(noticeDTOList)
                .newCount(count)
                .build();
    }

    public void addParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();
        Parent requester = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));

        Notice requested = noticeRepository.findByReceiverandSendertoJoinFamily(parent, requester);
        requested.setAccept(true);
        noticeRepository.save(requested);
        noticeRepository.delete(requested);
        List<Notice> otherRequested = noticeRepository.findAllReceiverBySender(requester);
        for(Notice otherRequest : otherRequested) {
            otherRequest.setAccept(true);
            noticeRepository.save(otherRequest);
            noticeRepository.delete(otherRequest);
        }

        requester.setStatus(Status.FIRST_LOGIN);
        requester.setFamily(family);
        parentRepository.save(requester);
    }

    public void declineParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Parent sender = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));

        Notice requested = noticeRepository.findByReceiverandSendertoJoinFamily(parent, sender);
        noticeRepository.delete(requested);

        if(noticeRepository.findAllBySender(parent).isEmpty()) parent.setStatus(Status.REJECT);
        parentRepository.save(parent);
    }

    public void readAll(Parent parent) {
        List<Notice> notices = noticeRepository.findAllByParentAndType(parent, 1);
        noticeRepository.deleteAll(notices);

        List<Notice> notice_grape = noticeRepository.findAllByParentAndType(parent, 3);
        noticeRepository.deleteAll(notice_grape);
    }
}
