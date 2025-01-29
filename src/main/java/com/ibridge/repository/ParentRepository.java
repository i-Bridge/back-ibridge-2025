package com.ibridge.repository;

import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.entity.Parent;
import com.ibridge.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    List<ParentResponseDTO.QuestionResponse> findQuestionsByParentId(Long parentId);
}
