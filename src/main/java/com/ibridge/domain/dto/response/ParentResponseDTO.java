package com.ibridge.domain.dto.response;

import lombok.*;

import java.util.List;


public class ParentResponseDTO {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetMyPageDTO {
        boolean noticeExist;
        String name;
        String familyName;
        List<ChildSimpleInfoDTO> children;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildIdDTO {
        Long childId;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildSimpleInfoDTO {
        Long childId;
        String childName;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildInfoDTO {
        Long id;
        String name;
        String birth;
        String gender;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParentIdDTO {
        Long parentId;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParentInfoDTO {
        Long id;
        String email;
        String name;
        boolean own;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetFamilyInfoDTO {
        String familyName;
        List<ParentInfoDTO> parents;
        List<ChildInfoDTO> children;
        Integer parentCount;
        Integer childCount;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoticeCheckDTO {
        List<NoticeDTO> notices;
        Integer newCount;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoticeDTO {
        Long noticeId;
        int type;
        Long senderId;
        String senderName;
        String time;
        boolean isAccept;
        Long subject;
        boolean isRead;
    }


}
