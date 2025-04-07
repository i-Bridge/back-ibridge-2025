package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParentHomeResponseDTO {
    private NoticeExistDTO noticeCount;
    private List<SubjectDTO> subjects;
}
