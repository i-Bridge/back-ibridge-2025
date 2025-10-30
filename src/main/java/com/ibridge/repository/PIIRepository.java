package com.ibridge.repository;

import com.ibridge.domain.dto.response.PIIResponseDTO;
import com.ibridge.domain.entity.PII;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PIIRepository extends JpaRepository<PII, Long> {
    List<PII> findAllByOrderByIdAsc();
}
