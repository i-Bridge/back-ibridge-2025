package com.ibridge.service;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Account;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Family;
import com.ibridge.domain.entity.Gender;
import com.ibridge.domain.entity.Parent;
import com.ibridge.repository.*;
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
    private final AnalysisRepository analysisRepository;
    private final QuestionRepository questionRepository;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


    public ParentResponseDTO.getMyPageDTO getMyPage(Long parentId) {
        Family family = parentRepository.getFamilyById(parentId);
        Parent parent = parentRepository.findById(parentId).get();

        List<ParentResponseDTO.childDTO> childDTOList = new ArrayList<>();
        List<Child> children = childRepository.findByFamily(family);
        for(Child child : children) {
            ParentResponseDTO.childDTO childDTO = ParentResponseDTO.childDTO.builder()
                    .childId(child.getId())
                    .name(child.getName())
                    .birth(child.getBirth().toString()).build();
            childDTOList.add(childDTO);
        }

        return ParentResponseDTO.getMyPageDTO.builder()
                .name(parent.getName())
                .account(parent.getEmail())
                .children(childDTOList).build();
    }

    public ParentResponseDTO.EditInfo editInfo(Long parentId, ParentRequestDTO.EditInfo request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        parent.setName(request.getName());
        Date newBirth = sdf.parse(request.getBirthday());
        parentRepository.save(parent);

        return ParentResponseDTO.EditInfo.builder()
                .parentId(parent.getId()).build();
    }

    public ParentResponseDTO.ParentHome getParentHomeData(Long parentId) {
        List<QuestionResponseDTO.QuestionResponse> questions = parentRepository.findQuestionsById(parentId);
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

    public QuestionResponseDTO addTempQuestion(Long parentId, QuestionRequestDTO request){
        Long questionId = questionRepository.saveQuestion(parentId, request.getQuestion());
        return new QuestionResponseDTO(questionId);
    }

    public ParentResponseDTO.PatchChildDTO patchChild(Long parentId, ParentRequestDTO.EditChildDTO request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Account account = parent.getAccount();

        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));
        child.setName(request.getName());
        child.setBirth(sdf.parse(request.getBirthday()));
        child.setGender(Gender.values()[request.getGender()]);
        childRepository.save(child);

        return ParentResponseDTO.PatchChildDTO.builder()
                .childId(child.getId()).build();
    }

    public void deleteChild(Long parentId, ParentRequestDTO.DeleteChildDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));

        childRepository.delete(child);
        return;
    }
}
