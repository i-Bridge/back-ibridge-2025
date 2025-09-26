package com.ibridge.service;

import com.ibridge.domain.dto.request.StartRequestDTO;
import com.ibridge.domain.dto.request.StartSignupNewRequestDTO;
import com.ibridge.domain.dto.response.FamilyExistDTO;
import com.ibridge.domain.dto.response.StartUserSelectionResponseDTO;
import com.ibridge.domain.entity.*;
import com.ibridge.repository.*;
import com.ibridge.domain.dto.response.StartResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StartService {
    private final ParentRepository parentRepository;
    private final FamilyRepository familyRepository;
    private final ChildRepository childRepository;
    private final NoticeRepository NoticeRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final ChildStatRepository childStatRepository;

    public StartResponseDTO signIn(String email, String name) {
        boolean isFirst = !parentRepository.existsByEmail(email);
        //isFirst가 true -> db에 저장
        if(isFirst) {
            Parent parent = Parent.builder()//gender 추가 해야 함
                    .name(new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8))
                    .email(email)
                    .build();
            parentRepository.save(parent);
            return new StartResponseDTO(isFirst);
        }
        return new StartResponseDTO(isFirst);
    }

    public FamilyExistDTO checkFamilyExistence(StartRequestDTO request, Parent parent) {
        Optional<Family> familyOptional = familyRepository.findByName(request.getFamilyName());

        if (familyOptional.isPresent()) {
            Family family = familyOptional.get();

            for (Parent p : family.getParents()) {
                Notice notice = Notice.builder()
                        .isAccept(false) // 기본값 미수락
                        .isRead(false)
                        .send_at(new Timestamp(System.currentTimeMillis()))
                        .receiver(p)
                        .sender(parent)
                        .type(2)
                        .build();

                NoticeRepository.save(notice);
            }

            return new FamilyExistDTO(true);
        }

        return new FamilyExistDTO(false);
    }

    public FamilyExistDTO checkFamilyDuplicate(StartRequestDTO request) {
        Optional<Family> familyOptional = familyRepository.findByName(request.getFamilyName());
        if (familyOptional.isEmpty()) {
            return new FamilyExistDTO(false);
        }
        return new FamilyExistDTO(true);
    }

    public void registerNewChildren(StartSignupNewRequestDTO request, String email) {
        Parent parent = parentRepository.findParentByEmail(email);
        if (parent == null) {
            throw new RuntimeException("존재하지 않는 이메일입니다: " + email);
        }

        Family family = familyRepository.save(Family.builder()
                .name(request.getFamilyName())
                .build());

        parent.setFamily(family);
        parentRepository.save(parent);

        List<String> subjectsPool = loadQuestionsFromFile();
        if (subjectsPool.isEmpty()) {
            throw new RuntimeException("질문 목록이 비어있습니다.");
        }

        Random random = new Random();

        List<Child> children = request.getChildren().stream()
                .map(childRequest -> {
                    Child child = Child.builder()
                            .name(childRequest.getName())
                            .gender(childRequest.getGender() == 0 ? Gender.MALE : Gender.FEMALE)
                            .birth(LocalDate.parse(childRequest.getBirth()))
                            .family(family)
                            .build();

                    List<Subject> subjectList = new ArrayList<>();
                    for (int i = 0; i < 14; i++) {
                        String questionText = subjectsPool.get(random.nextInt(subjectsPool.size()));

                        Question question = Question.builder()
                                .text(questionText)
                                .build();

                        Subject subject = Subject.builder()
                                .title(questionText)
                                .isAnswer(false)
                                .date(LocalDate.now().plusDays(i))
                                .questions(new ArrayList<>(List.of(question)))
                                .build();

                        question.setSubject(subject);
                        subject.setChild(child);

                        subjectList.add(subject);

                        subjectRepository.save(subject);
                        questionRepository.save(question);
                    }
                    child.setSubjects(subjectList);

                    ChildStat childStat = ChildStat.builder()
                            .child(child)
                            .type(PeriodType.CUMULATIVE)
                            .period(LocalDate.now())
                            .emotion(null)
                            .answerCount(0L)
                            .build();
                    return child;
                })
                .collect(Collectors.toList());
        childRepository.saveAll(children);
    }

    public StartUserSelectionResponseDTO getUserSelection(Parent parent) {
        // 가족 정보 조회
        Family family = parent.getFamily();
        boolean isAccept = true;
        if (family == null) {
            isAccept = false;
            List<Notice> Notice = NoticeRepository.findBySenderAndType(parent);
            return StartUserSelectionResponseDTO.builder()
                    .isAccepted(isAccept)
                    .isSend(!Notice.isEmpty())
                    .build();
        }
        else {        // 자녀 리스트 생성
            List<StartUserSelectionResponseDTO.ChildDTO> childDTOs = family.getChildren().stream()
                    .sorted(Comparator.comparing(Child::getBirth)) //  birth 기준 오름차순 정렬
                    .map(c -> StartUserSelectionResponseDTO.ChildDTO.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .birth(c.getBirth())
                            .gender(c.getGender() == Gender.MALE ? Gender.MALE : Gender.FEMALE)
                            .build())
                    .toList();

            return StartUserSelectionResponseDTO.builder()
                    .isAccepted(isAccept)
                    .familyName(family.getName())
                    .isSend(true) //임시로 false
                    .children(childDTOs)
                    .build();
        }
    }
    private List<String> loadQuestionsFromFile() {
        List<String> subjects = new ArrayList<>();
        try (InputStream inputStream = QuestionService.class.getClassLoader().getResourceAsStream("SubjectList.txt")) {
            if (inputStream == null) {
                System.out.println("파일을 찾을 수 없습니다.");
                throw new RuntimeException();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                subjects.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subjects;
    }


    @Transactional
    public void undoRequest(String email) {
        Parent parent = parentRepository.findParentByEmail(email);

        List<Notice> Notices = NoticeRepository.findAllBySender(parent);

        NoticeRepository.deleteAll(Notices);
    }


}
