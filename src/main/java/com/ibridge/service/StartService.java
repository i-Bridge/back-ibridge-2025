package com.ibridge.service;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSigninRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.FamilyExistDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import com.ibridge.domain.dto.response.StartResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    public StartResponseDTO signIn(String email, String name) {
        boolean isFirst = !parentRepository.existsByEmail(email);
        //isFirst가 true -> db에 저장
        if(isFirst) {
            Parent parent = Parent.builder()//gender 추가 해야 함
                    .name(new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8))
                    .email(email)
                    .build();
            parentRepository.save(parent);
            return new StartResponseDTO(isFirst);
        }
        return new StartResponseDTO(isFirst);
    }

    public FamilyExistDTO checkFamilyExistence(StartRequestDTO request, Parent parent) {
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
            }

            return new FamilyExistDTO(true);
        }

        return new FamilyExistDTO(false);
    }

    public FamilyExistDTO checkFamilyDuplicate(StartRequestDTO request) {
        Optional<Family> familyOptional = familyRepository.findByName(request.getFamilyName());
        if (familyOptional.isEmpty()) {
            return new FamilyExistDTO(false);
        }
        return new FamilyExistDTO(true);
    }

    public void registerNewChildren(StartSignupNewRequestDTO request, String email) {
        Parent parent = parentRepository.getParentByEmail(email);

        Family family = Family.builder()
                .name(request.getFamilyName())
                .build();
        familyRepository.save(family);

        parent.setFamily(family);
        parentRepository.save(parent);

        List<Child> children = request.getChildren().stream()
                .map(childRequest -> Child.builder()
                        .name(childRequest.getName())
                        .gender(childRequest.getGender() == 0 ? Gender.MALE : Gender.FEMALE)
                        .birth(LocalDate.parse(childRequest.getBirth()))
                        .family(family)
                        .build())
                .collect(Collectors.toList());

        childRepository.saveAll(children);
    }

    public StartUserSelectionResponseDTO getUserSelection(Parent parent) {
        // 가족 정보 조회
        Family family = parent.getFamily();
        boolean isAccept = true;
        if (family == null) {
            isAccept = false;
            Optional<ParentNotice> parentNotice = parentNoticeRepository.findBySenderAndType(parent);
            return StartUserSelectionResponseDTO.builder()
                    .isAccepted(isAccept)
                    .isSend(parentNotice.isPresent())
                    .build();
        }
        else {        // 자녀 리스트 생성
            List<StartUserSelectionResponseDTO.ChildDTO> childDTOs = family.getChildren().stream()
                    .sorted(Comparator.comparing(Child::getBirth)) //  birth 기준 오름차순 정렬
                    .map(c -> StartUserSelectionResponseDTO.ChildDTO.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .birth(c.getBirth())
                            .gender(c.getGender() == Gender.MALE ? Gender.MALE : Gender.FEMALE)
                            .build())
                    .toList();

            return StartUserSelectionResponseDTO.builder()
                    .isAccepted(isAccept)
                    .familyName(family.getName())
                    .isSend(true) //임시로 false
                    .children(childDTOs)
                    .build();
        }
    }
}
