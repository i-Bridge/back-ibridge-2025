package com.ibridge.service;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.entity.Account;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Gender;
import com.ibridge.domain.entity.Parent;
import com.ibridge.repository.AccountRepository;
import com.ibridge.repository.AnalysisRepository;
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

import static java.time.LocalDate.now;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;
    private final AccountRepository accountRepository;
    private final AnalysisRepository analysisRepository;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


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
        Date newBirth = sdf.parse(request.getBirthday());
        parent.setBirth(newBirth);
        parentRepository.save(parent);

        return ParentResponseDTO.EditInfo.builder()
                .parentId(parent.getId()).build();
    }

    public ParentResponseDTO.ParentHome getParentHomeData(Long parentId) {
        List<ParentResponseDTO.QuestionResponse> questions = parentRepository.findQuestionsById(parentId);
        return new ParentResponseDTO.ParentHome(now().toString(), questions);
    }

    public ParentResponseDTO.DeleteDTO deleteAccount(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        Account account = parent.getAccount();
        for(Child child : childRepository.findByAccount(account)) {
            childRepository.delete(child);
        }
        for(Parent parents : parentRepository.findByAccount(account)) {
            parentRepository.delete(parents);
        }
        accountRepository.delete(account);

        return ParentResponseDTO.DeleteDTO.builder()
                .deletedAt(new Date())
                .account(account.toString()).build();
    }

    public ParentResponseDTO.AddChildDTO addChild(Long parentId, ParentRequestDTO.AddChildDTO request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Account account = parent.getAccount();

        Child child = Child.builder()
                .account(account)
                .name(request.getName())
                .birth(sdf.parse(request.getBirthday()))
                .gender(Gender.values()[request.getGender()]).build();

        return ParentResponseDTO.AddChildDTO.builder()
                .childId(childRepository.save(child).getId()).build();
    }

    public QuestionAnalysisDTO getQuestionAnalysis(Long parentId, Long questionId){
        return analysisRepository.findAnalysisByQuestionId(parentId, questionId);
    }
}
