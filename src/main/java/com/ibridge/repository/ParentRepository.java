package com.ibridge.repository;

import com.ibridge.domain.dto.response.ParentResponseDTO;
import com.ibridge.domain.dto.response.QuestionResponseDTO;
import com.ibridge.domain.entity.Account;
import com.ibridge.domain.entity.Parent;
import com.ibridge.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    List<QuestionResponseDTO.QuestionResponse> findQuestionsById(Long parentId);

    @Query("SELECT p FROM Parent p WHERE p.account = :account")
    List<Parent> findByAccount(@Param("account") Account account);
}
