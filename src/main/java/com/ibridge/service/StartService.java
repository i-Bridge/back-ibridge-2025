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
            Parent parent = Parent.builder()
                    .email(email)
                    .name(name)
                    .build();
            parentRepository.save(parent);
        }
        return new StartResponseDTO(isFirst);
    }

    public StartResponseDTO checkFamilyExistence(StartRequestDTO request, Parent parent) {
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
                        .receiver(p)
                        .sender(parent) //임의로 null 설정
                        .notice(notice)
                        .build();

                parentNoticeRepository.save(parentNotice);
                notice.getParentNotices().add(parentNotice);
            }

            noticeRepository.save(notice);

            return new StartResponseDTO(true);
        }

        throw new IllegalArgumentException("해당 가족이 존재하지 않습니다.");
    }

    public StartResponseDTO registerNewFamily(StartSignupNewRequestDTO request) {
        Family family = Family.builder()
                .name(request.getParent().getFamilyName())
                .build();
        familyRepository.save(family);

        Parent parent = Parent.builder()
                .name("부모님 이름") // 임시 값
                .family(family)
                .build();
        parentRepository.save(parent);

        List<Child> children = request.getChildren().stream()
                .map(childRequest -> Child.builder()
                        .name(childRequest.getName())
                        .gender(childRequest.getGender() == 0 ? Gender.MALE : Gender.FEMALE)
                        .birth(Date.valueOf(childRequest.getBirth()))
                        .family(family)
                        .build())
                .collect(Collectors.toList());

        childRepository.saveAll(children);

        return new StartResponseDTO(true);
    }

    public StartUserSelectionResponseDTO getUserSelection(Long parentId) {
        // 부모 정보 조회
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("부모 정보를 찾을 수 없습니다."));

        // 가족 정보 조회
        Family family = parent.getFamily();
        if (family == null) {
            throw new RuntimeException("가족 정보가 없습니다.");
        }

        // 부모 리스트 생성
        List<StartUserSelectionResponseDTO.ParentDTO> parentDTOs = family.getParents().stream()
                .map(p -> StartUserSelectionResponseDTO.ParentDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .relation("부모") // 관계 설정 (추후 수정 가능)
                        .build())
                .collect(Collectors.toList());

        // 자녀 리스트 생성
        List<StartUserSelectionResponseDTO.ChildDTO> childDTOs = family.getChildren().stream()
                .map(c -> StartUserSelectionResponseDTO.ChildDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .birth(c.getBirth().toString()) // Date -> String 변환
                        .gender(c.getGender() == Gender.MALE ? 0 : 1) // Enum -> int 변환
                        .build())
                .collect(Collectors.toList());

        return StartUserSelectionResponseDTO.builder()
                .status(true) // 초대 수락 여부 (임시값, 로직 필요)
                .parents(parentDTOs)
                .children(childDTOs)
                .build();
    }
}
