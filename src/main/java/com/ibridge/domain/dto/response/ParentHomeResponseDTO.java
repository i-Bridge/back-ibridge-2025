package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.SubjectDTO;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ParentHomeResponseDTO {
    private Page<SubjectDTO> subjects;
    private Boolean hasNext;
}
