package com.ibridge.service;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.FamilyRepository;
import com.ibridge.repository.NoticeRepository;
import com.ibridge.util.ApiResponse;
import com.ibridge.util.CustomOAuth2User;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.repository.ParentRepository;
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

    public StartResponseDTO signIn(CustomOAuth2User oAuth2User) {
        String email = oAuth2User.getEmail();
        boolean isFirst = !parentRepository.existsByEmail(email);

        return new StartResponseDTO(isFirst);
    }

    public StartResponseDTO checkFamilyExistence(StartRequestDTO request) {
        Optional<Family> familyOptional = familyRepository.findByName(request.getFamilyName());

        if (familyOptional.isPresent()) {
            Family family = familyOptional.get();

            // 부모들에게 초대 알림 생성
            for (Parent parent : family.getParents()) {
                Notice notice = Notice.builder()
                        .type(1) // 초대 알림 유형 (임의로 1 설정)
                        .sender("System") // 시스템에서 보낸 것으로 설정
                        .build();
                notice.getReceivers().add(parent); // 부모 추가
                noticeRepository.save(notice);
            }

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
                .name("부모님 이름") // 부모 이름이 없으므로 임시 값
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
