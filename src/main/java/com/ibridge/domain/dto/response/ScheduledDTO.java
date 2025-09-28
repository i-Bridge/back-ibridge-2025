package com.ibridge.domain.dto.response;

import com.ibridge.domain.dto.SubjectDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledDTO {
    List<SubjectDTO> subjects;
}
