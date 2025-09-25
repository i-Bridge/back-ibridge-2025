package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.SubjectDTO;
import com.ibridge.domain.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CategorySubjectDTO {
    List<SubjectDTO> subjects;
}
