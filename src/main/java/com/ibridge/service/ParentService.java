package com.ibridge.service;

import com.ibridge.domain.dto.request.NoticeRequestDTO;
import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.*;
import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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

        return ParentHomeResponseDTO.builder()
                .subjects(subjects)
                .name(child.getName())
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
        for(int i = 1;i<32;i++){
            result.add(arr[i]);
        }
        return readSubjectsResponseDTO.builder()
                .month(result)
                .build();
    }
    public void openNotice(NoticeRequestDTO noticeRequestDTO) {
        noticeRepository.deleteById(noticeRequestDTO.getNoticeId());
    }

    public AnalysisResponseDTO getDefaultAnalysis(Long childId, LocalDate today) {
        Child child = childRepository.findById(childId).orElse(null);
        Long cumulative = childStatRepository.findSumByChildAndType(child, PeriodType.MONTH);
        YearMonth yearMonth = YearMonth.from(today);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Emotion> emotions = childStatRepository.findEmotionsByChildAndMonth(child, start, end);

        List<String> periodList = new ArrayList<>();

        for(int i = 6; i >= 0; i--) {
            periodList.add(today.minusDays(i).toString()); // "YYYY-MM-DD"
        }

        List<Long> cumList = childStatRepository.findAnswerCountsByChildAndPeriodList(child, periodList);

        List<KeywordDTO> keywordList;
        PageRequest top7 = PageRequest.of(0,7);
        keywordList = childPositiveBoardRepository.findKeywordsAndPositivesByMonth(child, top7);

        return AnalysisResponseDTO.builder()
                .cumulative(cumulative)
                .emotions(emotions)
                .cumList(cumList)
                .keywords(keywordList)
                .build();
    }

    public AnalysisResponseDTO getEmotions(Long childId, LocalDate today) {
        Child child = childRepository.findById(childId).orElse(null);
        YearMonth yearMonth = YearMonth.from(today);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Emotion> emotions = childStatRepository.findEmotionsByChildAndMonth(child, start, end);

        return AnalysisResponseDTO.builder()
                .emotions(emotions)
                .build();
    }








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
                Analysis analysis = analysisRepository.findByQuestionId(question.getId()).get();
                analysisRepository.delete(analysis);
                questionRepository.delete(question);
            }
            subjectRepository.delete(subject);
        }
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
