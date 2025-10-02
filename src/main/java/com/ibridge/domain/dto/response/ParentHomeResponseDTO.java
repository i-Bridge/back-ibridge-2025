package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.SubjectDTO;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentHomeResponseDTO {
    private List<SubjectDTO> subjects;
    private Boolean hasNext;
    private Integer page;
}
