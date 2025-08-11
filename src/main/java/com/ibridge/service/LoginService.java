package com.ibridge.service;

import com.ibridge.domain.entity.Parent;
import com.ibridge.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final ParentRepository parentRepository;

    public Parent getParentFromHeader(String email) {
        return parentRepository.findParentByEmail(email);
    }
}
