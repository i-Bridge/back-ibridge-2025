package com.ibridge.service;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.ParentHomeResponseDTO;
import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;
    private final AnalysisRepository analysisRepository;
    private final QuestionRepository questionRepository;
    private final FamilyRepository famliyRepository;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final NoticeRepository noticeRepository;
    private final ParentNoticeRepository parentNoticeRepository;
    private final FamilyRepository familyRepository;


    public ParentResponseDTO.getMyPageDTO getMyPage(Long parentId) {
        Parent parent = parentRepository.findById(parentId).get();
        Family family = parent.getFamily();

        List<ParentResponseDTO.childDTO> childDTOList = new ArrayList<>();
        List<Child> children = childRepository.findByFamily(family);
        for(Child child : children) {
            ParentResponseDTO.childDTO childDTO = ParentResponseDTO.childDTO.builder()
                    .childId(child.getId())
                    .name(child.getName())
                    .birth(child.getBirth().toString()).build();
            childDTOList.add(childDTO);
        }

        return ParentResponseDTO.getMyPageDTO.builder()
                .name(parent.getName())
                .account(parent.getEmail())
                .children(childDTOList).build();
    }

    public ParentResponseDTO.EditInfo editInfo(Long parentId, ParentRequestDTO.EditInfo request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        parent.setName(request.getName());
        parentRepository.save(parent);

        return ParentResponseDTO.EditInfo.builder()
                .parentId(parent.getId()).build();
    }

    public void deleteAccount(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        if(parentRepository.getParentCount(family) > 1) {
            parentRepository.deleteById(parentId);
            return;
        }

        for(Child child : childRepository.findByFamily(family)) {
            childRepository.delete(child);
        }
        famliyRepository.delete(family);
    }

    public ParentResponseDTO.AddChildDTO addChild(Long parentId, ParentRequestDTO.AddChildDTO request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        Child child = Child.builder()
                .family(family)
                .name(request.getName())
                .birth(sdf.parse(request.getBirthday()))
                .gender(Gender.values()[request.getGender()]).build();

        return ParentResponseDTO.AddChildDTO.builder()
                .childId(childRepository.save(child).getId()).build();
    }

    public ParentResponseDTO.PatchChildDTO patchChild(Long parentId, ParentRequestDTO.EditChildDTO request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));
        child.setName(request.getName());
        child.setBirth(sdf.parse(request.getBirthday()));
        child.setGender(Gender.values()[request.getGender()]);
        childRepository.save(child);

        return ParentResponseDTO.PatchChildDTO.builder()
                .childId(child.getId()).build();
    }

    public void deleteChild(ParentRequestDTO.DeleteChildDTO request) {
        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));

        childRepository.delete(child);
        return;
    }

    public ParentHomeResponseDTO getParentHome(Long childId, String dateStr) {
        LocalDate date = (dateStr != null && !dateStr.isEmpty())
                ? LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : LocalDate.now();

        // 자녀가 있는지
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("해당 자녀를 찾을 수 없습니다."));

        // 해당 자녀의 해당 월 모든 질문 조회
        List<Question> monthlyQuestions = questionRepository.findByChildAndMonth(child, date.getYear(), date.getMonthValue());

        // 날짜별 isAnswer=true 개수 집계
        Map<Integer, Long> dailyAnswerCountMap = monthlyQuestions.stream()
                .filter(Question::isAnswer)
                .collect(Collectors.groupingBy(
                        q -> q.getDate().toLocalDateTime().getDayOfMonth(),
                        Collectors.counting()
                ));

        List<ParentHomeResponseDTO.AnswerCountDTO> answerCountDTOs = new ArrayList<>();
        for (int i = 1; i <= date.lengthOfMonth(); i++) {
            answerCountDTOs.add(ParentHomeResponseDTO.AnswerCountDTO.builder()
                    .count(dailyAnswerCountMap.getOrDefault(i, 0L).intValue())
                    .build());
        }

        // 특정 날짜의 질문 목록 조회
        List<Question> dailyQuestions = questionRepository.findByChildAndDate(child, date);
        List<ParentHomeResponseDTO.QuestionDTO> questionDTOs = dailyQuestions.stream()
                .map(q -> ParentHomeResponseDTO.QuestionDTO.builder()
                        .questionId(q.getId())
                        .question(q.getText())
                        .type(q.getType())
                        .time(q.getTime())
                        .isAnswer(q.isAnswer())
                        .date(q.getDate())
                        .build())
                .collect(Collectors.toList());

        return ParentHomeResponseDTO.builder()
                .answers(answerCountDTOs)
                .questions(questionDTOs)
                .build();
    }

    public ParentResponseDTO.NoticeCheckDTO noticeCheck(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        List<ParentResponseDTO.NoticeDTO> noticeDTOList = new ArrayList<>();
        List<ParentNotice> noticesForParent = parentNoticeRepository.findAllByParent(parent);
        for(ParentNotice notice : noticesForParent) {
            Notice specNotice = notice.getNotice();
            Boolean isAccept = null;
            if(specNotice.getType() == 1) {
                isAccept = notice.isAccept();
            }
            ParentResponseDTO.NoticeDTO tempDTO = ParentResponseDTO.NoticeDTO.builder()
                    .parentId(notice.getSender().getId())
                    .type(specNotice.getType())
                    .isAccept(isAccept)
                    .parentName(notice.getSender().getName())
                    .noticeId(specNotice.getId()).build();
            noticeDTOList.add(tempDTO);
        }

        return ParentResponseDTO.NoticeCheckDTO.builder().notices(noticeDTOList).build();
    }

    public void getParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();
        Parent requester = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));

        ParentNotice requested = parentNoticeRepository.findByReceiverandSendertoJoinFamily(parent, requester);
        requested.setAccept(true);
        parentNoticeRepository.save(requested);

        family.getParents().add(requester);
        familyRepository.save(family);
    }

    public void deleteNotice(Long parentId, ParentRequestDTO.DeleteNoticeDTO reqeust) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Notice notice = noticeRepository.findById(reqeust.getNoticeId()).orElseThrow(() -> new RuntimeException("Notice not found"));
        ParentNotice parentNotice = parentNoticeRepository.findByNoticeandReceiver(notice, parent);

        List<ParentNotice> forOtherReceivers = parentNoticeRepository.findAllReceiverByNotice(notice);
        boolean flag = false;
        if(forOtherReceivers.size() > 1) {
            flag = true;
        }
        parentNoticeRepository.delete(parentNotice);
        if(flag) noticeRepository.delete(parentNotice.getNotice());
    }

    public void deleteNoticeAll(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        List<ParentNotice> parentNotices = parentNoticeRepository.findAllByParent(parent);
        for(ParentNotice parentNotice : parentNotices) {
            List<ParentNotice> forOtherReceivers = parentNoticeRepository.findAllReceiverByNotice(parentNotice.getNotice());
            boolean flag = false;
            if(forOtherReceivers.size() > 1) {
                flag = true;
            }
            parentNoticeRepository.delete(parentNotice);
            if(flag) noticeRepository.delete(parentNotice.getNotice());
        }

    }
}
