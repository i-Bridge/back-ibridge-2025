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

//현호
    public ParentResponseDTO.GetMyPageDTO getMyPage(Long parentId) {
    Parent parent = parentRepository.findById(parentId).get();
    Family family = parent.getFamily();

    List<ParentResponseDTO.ChildIdDTO> childDTOList = new ArrayList<>();
    List<Child> children = childRepository.findAllByFamily(family);
    for(Child child : children) {
        ParentResponseDTO.ChildIdDTO childDTO = ParentResponseDTO.ChildIdDTO.builder()
                .childId(child.getId()).build();
        childDTOList.add(childDTO);
    }

    return ParentResponseDTO.GetMyPageDTO.builder()
            .name(parent.getName())
            .familyName(family.getName())
            .children(childDTOList).build();
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
        family.setName(request.getFamilyName());
        return;
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
                .birth(sdf.parse(request.getBirthday()))
                .gender(Gender.values()[request.getGender()]).build();

        return ParentResponseDTO.ChildIdDTO.builder()
                .childId(childRepository.save(child).getId()).build();
    }

    public ParentResponseDTO.ChildIdDTO patchChild(Long parentId, ParentRequestDTO.EditChildDTO request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));
        child.setName(request.getName());
        child.setBirth(sdf.parse(request.getBirthday()));
        child.setGender(Gender.values()[request.getGender()]);
        childRepository.save(child);

        return ParentResponseDTO.ChildIdDTO.builder()
                .childId(child.getId()).build();
    }

    public void deleteChild(ParentRequestDTO.DeleteChildDTO request) {
        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));

        childRepository.delete(child);
        return;
    }



    //알림
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
        List<ParentNotice> otherRequested = parentNoticeRepository.findAllReceiverByNotice(requested.getNotice());
        for(ParentNotice otherRequest : otherRequested) {
            otherRequest.setAccept(true);
            parentNoticeRepository.save(otherRequest);
        }

        family.getParents().add(requester);
        familyRepository.save(family);
    }
}
