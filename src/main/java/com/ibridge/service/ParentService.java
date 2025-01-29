package com.ibridge.service;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.entity.Account;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Parent;
import com.ibridge.repository.AccountRepository;
import com.ibridge.repository.ChildRepository;
import com.ibridge.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;

    public ParentResponseDTO.getMyPageDTO getMyPage(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        Account account = parent.getAccount();
        List<Child> children = childRepository.findByAccount(account);
        List<ParentResponseDTO.childDTO> childDTOList = new ArrayList<>();
        for(Child child : children) {
            ParentResponseDTO.childDTO childDTO = ParentResponseDTO.childDTO.builder()
                    .childId(child.getId())
                    .name(child.getName())
                    .birth(child.getBirth().toString()).build();
            childDTOList.add(childDTO);
        }

        return ParentResponseDTO.getMyPageDTO.builder()
                .name(parent.getName())
                .account(account.toString())
                .birth(parent.getBirth().toString())
                .relation(parent.getRelation().toString())
                .children(childDTOList).build();
    }

    public ParentResponseDTO.EditInfo editInfo(Long parentId, ParentRequestDTO.EditInfo request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        parent.setName(request.getName());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date newBirth = sdf.parse(request.getBirthday());
        parent.setBirth(newBirth);
        parentRepository.save(parent);

        return ParentResponseDTO.EditInfo.builder()
                .parentId(parent.getId()).build();
    }
}
