package com.ibridge.service;

import com.ibridge.domain.dto.request.ParentRequestDTO;
import com.ibridge.domain.dto.request.QuestionRequestDTO;
import com.ibridge.domain.dto.response.ParentHomeResponseDTO;
import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.dto.response.QuestionAnalysisDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;
    private final AnalysisRepository analysisRepository;
    private final QuestionRepository questionRepository;
    private final FamilyRepository famliyRepository;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final NoticeRepository noticeRepository;


    public ParentResponseDTO.getMyPageDTO getMyPage(Long parentId) {
        Parent parent = parentRepository.findById(parentId).get();
        Family family = parent.getFamily();

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
        parentRepository.save(parent);

        return ParentResponseDTO.EditInfo.builder()
                .parentId(parent.getId()).build();
    }

    public void deleteAccount(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        if(parentRepository.getParentCount(family) > 1) {
            parentRepository.deleteById(parentId);
            return;
        }

        for(Child child : childRepository.findByFamily(family)) {
            childRepository.delete(child);
        }
        famliyRepository.delete(family);
    }

    public ParentResponseDTO.AddChildDTO addChild(Long parentId, ParentRequestDTO.AddChildDTO request) throws ParseException {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        Child child = Child.builder()
                .family(family)
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
        Family family = parent.getFamily();

        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));
        child.setName(request.getName());
        child.setBirth(sdf.parse(request.getBirthday()));
        child.setGender(Gender.values()[request.getGender()]);
        childRepository.save(child);

        return ParentResponseDTO.PatchChildDTO.builder()
                .childId(child.getId()).build();
    }

    public void deleteChild(ParentRequestDTO.DeleteChildDTO request) {
        Child child = childRepository.findById(request.getChildId()).orElseThrow(() -> new RuntimeException("Child not found"));

        childRepository.delete(child);
        return;
    }

    public ParentHomeResponseDTO getParentHome(Long childId, String dateStr) {
        LocalDate date = (dateStr != null && !dateStr.isEmpty())
                ? LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : LocalDate.now();

        // 자녀가 있는지
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("해당 자녀를 찾을 수 없습니다."));

        // 해당 자녀의 해당 월 모든 질문 조회
        List<Question> monthlyQuestions = questionRepository.findByChildAndMonth(child, date.getYear(), date.getMonthValue());

        // 날짜별 isAnswer=true 개수 집계
        Map<Integer, Long> dailyAnswerCountMap = monthlyQuestions.stream()
                .filter(Question::isAnswer)
                .collect(Collectors.groupingBy(
                        q -> q.getTime().toLocalDateTime().getDayOfMonth(),
                        Collectors.counting()
                ));

        // 응답 데이터 생성
        List<ParentHomeResponseDTO.AnswerCountDTO> answerCountDTOs = new ArrayList<>();
        for (int i = 1; i <= date.lengthOfMonth(); i++) {
            answerCountDTOs.add(ParentHomeResponseDTO.AnswerCountDTO.builder()
                    .count(dailyAnswerCountMap.getOrDefault(i, 0L).intValue())
                    .build());
        }

        // 특정 날짜의 질문 목록 조회
        List<Question> dailyQuestions = questionRepository.findByChildAndDate(child, date);
        List<ParentHomeResponseDTO.QuestionDTO> questionDTOs = dailyQuestions.stream()
                .map(q -> ParentHomeResponseDTO.QuestionDTO.builder()
                        .questionId(q.getId())
                        .question(q.getText())
                        .type(q.getType())
                        .time(q.getTime().toString())
                        .isAnswer(q.isAnswer())
                        .build())
                .collect(Collectors.toList());

        return ParentHomeResponseDTO.builder()
                .answers(answerCountDTOs)
                .questions(questionDTOs)
                .build();
    }

    public ParentResponseDTO.NoticeCheckDTO noticeCheck(Long parentId) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));

        List<ParentResponseDTO.NoticeDTO> noticeDTOList = new ArrayList<>();
        List<Notice> notices = noticeRepository.findByReceiver(parent);
        for(Notice notice : notices) {
            ParentResponseDTO.NoticeDTO noticeDTO = new ParentResponseDTO.NoticeDTO();
            //작성 필요
            noticeDTOList.add(noticeDTO);
        }

        return ParentResponseDTO.NoticeCheckDTO.builder().notices(noticeDTOList).build();
    }

    public void getParentintoFamily(Long parentId, ParentRequestDTO.getParentintoFamilyDTO request) {
        Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
        Family family = parent.getFamily();

        Parent requester = parentRepository.findById(request.getParentId()).orElseThrow(() -> new RuntimeException("Sender not found"));
        family.getParents().add(requester);
    }
}
