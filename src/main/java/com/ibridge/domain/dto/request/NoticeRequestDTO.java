package com.ibridge.domain.dto.request;

import com.ibridge.domain.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeRequestDTO {
    Long noticeId;
}
