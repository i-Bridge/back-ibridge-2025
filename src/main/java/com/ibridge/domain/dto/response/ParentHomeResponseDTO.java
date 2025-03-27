package com.ibridge.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Builder
public class ParentHomeResponseDTO {
    private NoticeCountDTO noticeCount;
    private List<SubjectDTO> subjects;
}
