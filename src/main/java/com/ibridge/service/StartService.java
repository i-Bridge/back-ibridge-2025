package com.ibridge.service;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSigninRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import com.ibridge.domain.dto.response.StartResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StartService {
    private final ParentRepository parentRepository;
    private final FamilyRepository familyRepository;
    private final NoticeRepository noticeRepository;
    private final ChildRepository childRepository;
    private final ParentNoticeRepository parentNoticeRepository;

    public StartResponseDTO signIn(StartSigninRequestDTO startSigninRequestDTO) {
        String email = startSigninRequestDTO.getEmail();
        String name = startSigninRequestDTO.getName();
        boolean isFirst = !parentRepository.existsByEmail(email);
        //isFirst가 true -> db에 저장
        if(isFirst) {
            Parent parent = Parent.builder()//gender 추가 해야 함
                    .name(name)
                    .email(email)
                    .build();
            parentRepository.save(parent);
        }
        return new StartResponseDTO(isFirst);
    }

    public void checkFamilyExistence(StartRequestDTO request, Parent parent) {
        Optional<Family> familyOptional = familyRepository.findByName(request.getFamilyName());

        if (familyOptional.isPresent()) {
            Family family = familyOptional.get();

            Notice notice = Notice.builder()
                    .type(2) //초대 알림 타입 2
                    .build();
            noticeRepository.save(notice);
            for (Parent p : family.getParents()) {
                ParentNotice parentNotice = ParentNotice.builder()
                        .isAccept(false) // 기본값 미수락
                        .isRead(false)
                        .send_at(new Timestamp(System.currentTimeMillis()))
                        .receiver(p)
                        .sender(parent)
                        .notice(notice)
                        .build();

                parentNoticeRepository.save(parentNotice);
                notice.getParentNotices().add(parentNotice);
            }

            noticeRepository.save(notice);

            return;
        }

        throw new IllegalArgumentException("해당 가족이 존재하지 않습니다.");
    }

    public Boolean checkFamilyDuplicate(StartRequestDTO request, Parent parent) {
        Optional<Family> familyOptional = familyRepository.findByName(request.getFamilyName());
        if (familyOptional.isEmpty()) {
            List<Parent> parents = new ArrayList<>();
            parents.add(parent);
            Family family = Family.builder()
                    .name(request.getFamilyName())
                    .parents(parents)
                    .build();
            familyRepository.save(family);
            return true;
        }
        throw new IllegalArgumentException("동일한 가족 이름이 존재합니다.");
    }

    public void registerNewChildren(StartSignupNewRequestDTO request, String email) {
        Parent parent = parentRepository.getParentByEmail(email);
        Family family = parent.getFamily();

        List<Child> children = request.getChildren().stream()
                .map(childRequest -> Child.builder()
                        .name(childRequest.getName())
                        .gender(childRequest.getGender() == 0 ? Gender.MALE : Gender.FEMALE)
                        .birth(Date.valueOf(childRequest.getBirth()))
                        .family(family)
                        .build())
                .collect(Collectors.toList());

        childRepository.saveAll(children);
    }

    public StartUserSelectionResponseDTO getUserSelection(Parent parent) {
        // 가족 정보 조회
        Family family = parent.getFamily();
        if (family == null) {
            throw new RuntimeException("가족 정보가 없습니다.");
        }
        ParentNotice parentNotice = parentNoticeRepository.findBySender(parent);
        // 자녀 리스트 생성
        List<StartUserSelectionResponseDTO.ChildDTO> childDTOs = family.getChildren().stream()
                .map(c -> StartUserSelectionResponseDTO.ChildDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .birth(c.getBirth().toString())
                        .gender(c.getGender() == Gender.MALE ? Gender.MALE : Gender.FEMALE)
                        .build())
                .collect(Collectors.toList());

        return StartUserSelectionResponseDTO.builder()
                .isAccepted(parentNotice.isAccept())
                .isSend(false) //임시로 false
                .children(childDTOs)
                .build();
    }
}
