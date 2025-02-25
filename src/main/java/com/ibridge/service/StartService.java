package com.ibridge.service;

<<<<<<< HEAD
import com.ibridge.domain.dto.response.UserSelectionResponseDTO;
import com.ibridge.domain.entity.Account;
import com.ibridge.domain.entity.User;
=======
import com.ibridge.util.CustomOAuth2User;
import com.ibridge.domain.dto.response.StartResponseDTO;
import com.ibridge.repository.ParentRepository;
>>>>>>> ec0ae5e73a03075c71675037013c48112e6cc55d
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
