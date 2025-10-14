package com.ibridge.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ChildResponseDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getQuestionDTO {
        String childName;
        Long grapeBunches;
        Long grapePieces;
        boolean rewardAvailable;
        Integer emotion;
        boolean emotionDone;
        boolean specifiedDone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class bunchAvailableDTO {
        Long grapeBunches;
        Long grapePieces;
        boolean available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getRewardDTO {
        Long grape;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getStoreDTO {
        Long grape;
        List<getStoreItemsDTO> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getStoreItemsDTO {
        Long id;
        String name;
        Long cost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class purchaseDTO {
        Long grape;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getPredesignedQuestionDTO {
        Long subjectId;
        List<ConversationDTO> questions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationDTO {
        String ai;
        String user;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getNewQuestionDTO {
        Long subjectId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getAI {
        boolean isFinished;
        String ai;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getPresignedURLDTO {
        String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class finishedDTO{
        Long grape;
    }
}
