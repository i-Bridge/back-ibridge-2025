package com.ibridge.service;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.entity.Family;
import com.ibridge.domain.entity.Notice;
import com.ibridge.domain.entity.Parent;
import com.ibridge.repository.FamilyRepository;
import com.ibridge.repository.NoticeRepository;
import com.ibridge.util.ApiResponse;
import com.ibridge.util.CustomOAuth2User;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StartService {
    private final ParentRepository parentRepository;
    private final FamilyRepository familyRepository;
    private final NoticeRepository noticeRepository;
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

}
