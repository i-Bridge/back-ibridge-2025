package com.ibridge.service;

import com.ibridge.domain.dto.response.UserSelectionResponseDTO;
import com.ibridge.domain.entity.User;
import com.ibridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StartService {
    private final UserRepository userRepository;

    public UserSelectionResponseDTO getUserSelection(Long accountId){
        List<User> parents = userRepository.findParentsByAccountId(accountId);
        List<User> children = userRepository.findChildrenByAccountId(accountId);

        List<UserSelectionResponseDTO.ParentInfo> parentDTOs = parents.stream()
                .map(p -> new UserSelectionResponseDTO.ParentInfo(p.getId(), p.getName(), p.getRelation()))
                .collect(Collectors.toList());

        List<UserSelectionResponseDTO.ChildInfo> childDTOs = children.stream()
                .map(c -> new UserSelectionResponseDTO.ChildInfo(c.getId(), c.getName(), c.getBirth(), c.getGender()))
                .collect(Collectors.toList());

        return new UserSelectionResponseDTO(parentDTOs, childDTOs);
    }
}
