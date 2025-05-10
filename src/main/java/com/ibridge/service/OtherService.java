package com.ibridge.service;

import com.ibridge.domain.dto.response.SubjectResponseDTO;
import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Subject;
import com.ibridge.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OtherService {
    private final ChildRepository childRepository;
    private final QuestionService questionService;

    public void setSubject() {
        List<Child> children = childRepository.findAll();
        for (Child child : children) {
            questionService.rerollQuestion(child.getId(), LocalDate.now());
        }
    }
}
