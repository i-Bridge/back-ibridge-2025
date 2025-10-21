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
        Long childId;
        String childName;
        String childBirth;
        int childGender;
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
        Long parentId;
        String parentName;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetFamilyInfoDTO {
        String familyName;
        List<ParentInfoDTO> parents;
        List<ChildInfoDTO> children;
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
