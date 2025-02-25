package com.ibridge.service;

import com.ibridge.util.CustomOAuth2User;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StartService {
    private final ParentRepository parentRepository;

    public StartResponseDTO signIn(CustomOAuth2User oAuth2User) {
        String email = oAuth2User.getEmail();
        boolean isFirst = !parentRepository.existsByEmail(email);

        return new StartResponseDTO(isFirst);
    }
}
