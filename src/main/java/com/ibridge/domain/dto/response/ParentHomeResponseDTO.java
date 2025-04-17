package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.SubjectDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParentHomeResponseDTO {
    private List<SubjectDTO> subjects;
}
