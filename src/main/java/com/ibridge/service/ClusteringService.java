package com.ibridge.service;

import com.ibridge.domain.entity.*;
import com.ibridge.repository.ChildPositiveBoardRepository;
import com.ibridge.repository.NoticeRepository;
import com.ibridge.repository.ParentRepository;
import com.ibridge.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClusteringService {
    private final GptService gptService;
    private final SubjectRepository subjectRepository;
    private final ChildPositiveBoardRepository childPositiveBoardRepository;
    private final ParentRepository parentRepository;
    private final NoticeRepository noticeRepository;

    @Async
    @Transactional
    public void clustering(Child child, List<Subject> subjectList) {
        //군집화 진행
        Map<String, List<Long>> categorized = gptService.categorizeSubjects(subjectList);
        for (Map.Entry<String, List<Long>> entry : categorized.entrySet()) {
            String keyword = entry.getKey();
            List<Long> subjectIds = entry.getValue();

            // SubjectRepository 이용해서 batch 조회
            List<Subject> subjects = subjectRepository.findAllById(subjectIds);

            for (Subject subject : subjects) {
                subject.setKeyword(keyword);

                Optional<ChildPositiveBoard> childPositiveBoard = childPositiveBoardRepository.findByChildAndKeyword(child, keyword, LocalDate.now());
                if(childPositiveBoard.isPresent()) {
                    Long newPositive = childPositiveBoard.get().getPositive() * childPositiveBoard.get().getKeywordCount() + subject.getPositive();
                    childPositiveBoard.get().setPositive(newPositive / (childPositiveBoard.get().getKeywordCount() + 1));

                    childPositiveBoard.get().setKeywordCount(childPositiveBoard.get().getKeywordCount() + 1);

                    childPositiveBoardRepository.save(childPositiveBoard.get());
                }
                else {
                    ChildPositiveBoard newKeyword = ChildPositiveBoard.builder()
                            .keyword(keyword)
                            .child(child)
                            .keywordCount(1L)
                            .period(LocalDate.now())
                            .positive(Long.valueOf(subject.getPositive()))
                            .build();
                    childPositiveBoardRepository.save(newKeyword);
                }
            }
        }

        //알람 전송
        List<Parent> parents = parentRepository.findAllByFamily(child.getFamily());
        for(Parent parent : parents) {
            Notice notice = Notice.builder()
                    .child(child)
                    .send_at(Timestamp.valueOf(LocalDateTime.now()))
                    .type(3)
                    .receiver(parent).build();
            noticeRepository.save(notice);
        }
    }
}
