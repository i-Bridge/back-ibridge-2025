package com.ibridge.service;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.NoticeExistDTO;
import com.ibridge.domain.dto.response.ParentHomeResponseDTO;
import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;
    private final FamilyRepository famliyRepository;
    private final NoticeRepository noticeRepository;
    private final ParentNoticeRepository parentNoticeRepository;
    private final SubjectRepository subjectRepository;

    public ParentHomeResponseDTO getParentHome(Long childId, LocalDate date, String email) {
        List<SubjectDTO> subjects = subjectRepository.findByChildIdAndDate(childId, date).stream()
                .map(subject -> new SubjectDTO(subject.getId(), subject.getTitle(), subject.isAnswer()))
                .collect(Collectors.toList());

        return ParentHomeResponseDTO.builder()
                .subjects(subjects)
                .build();
    }

//현호
    public ParentResponseDTO.GetMyPageDTO getMyPage(Long parentId) {
    Parent parent = parentRepository.findById(parentId).get();
    List<ParentNotice> parentNotice = parentNoticeRepository.findAllByParent(parent);
    boolean noticeExist = false;
    for(ParentNotice notice : parentNotice) {
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
                .birth(LocalDate.parse(request.getBirthday()))
                .gender(Gender.values()[request.getGender()]).build();

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

        childRepository.delete(child);
        return;
    }



    //알림
    public ParentResponseDTO.NoticeCheckDTO getNotice(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        List<ParentResponseDTO.NoticeDTO> noticeDTOList = new ArrayList<>();
        List<ParentNotice> noticesForParent = parentNoticeRepository.findAllByParent(parent);
        for(ParentNotice parentNotice : noticesForParent) {
            Notice notice = parentNotice.getNotice();

            if((notice.getType() == 1 || notice.getType() == 3) && parentNotice.isRead()) {
                parentNoticeRepository.delete(parentNotice);
                if(parentNoticeRepository.CountByNotice(notice) == 0) noticeRepository.delete(notice);
            }
            else {
                parentNotice.setRead(true);
                parentNoticeRepository.save(parentNotice);

                ParentResponseDTO.NoticeDTO tempDTO = ParentResponseDTO.NoticeDTO.builder()
                        .senderId(parentNotice.getSender().getId())
                        .type(notice.getType())
                        .isAccept(parentNotice.isAccept())
                        .senderName(parentNotice.getSender().getName())
                        .noticeId(notice.getId()).build();
                noticeDTOList.add(tempDTO);
            }
        }

        return ParentResponseDTO.NoticeCheckDTO.builder().notices(noticeDTOList).build();
    }

    public void addParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();
        Parent requester = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));

        ParentNotice requested = parentNoticeRepository.findByReceiverandSendertoJoinFamily(parent, requester);
        requested.setAccept(true);
        parentNoticeRepository.save(requested);
        parentNoticeRepository.delete(requested);
        List<ParentNotice> otherRequested = parentNoticeRepository.findAllReceiverByNotice(requested.getNotice());
        for(ParentNotice otherRequest : otherRequested) {
            otherRequest.setAccept(true);
            parentNoticeRepository.save(otherRequest);
            parentNoticeRepository.delete(otherRequest);
        }

        requester.setFamily(family);
        noticeRepository.delete(requested.getNotice());
        parentRepository.save(requester);
    }

    public void declineParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Parent sender = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));

        ParentNotice requested = parentNoticeRepository.findByReceiverandSendertoJoinFamily(parent, sender);
        parentNoticeRepository.delete(requested);
    }
}
