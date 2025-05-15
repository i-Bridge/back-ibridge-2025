package com.ibridge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class gptService {
    @Value("${openai.api.key}")
    private String apiKey;

    private final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";

    public String generateQuestionFromAnswer(String childAnswer) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 본문 구성
        Map<String, Object> message = Map.of("role", "user", "content",
                "다음은 아이의 답변이야: \"" + childAnswer + "\". 이 답변에 대해 후속 질문 하나를 만들어줘.");

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(message),
                "temperature", 0.7 //창의성 조절 파라미터라고 하네요
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GPT_API_URL,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null || !responseBody.containsKey("choices")) {
            throw new RuntimeException("GPT 응답이 비어 있거나 'choices' 키가 없습니다.");
        }

        List<?> choices = (List<?>) responseBody.get("choices");
        if (choices.isEmpty()) {
            throw new RuntimeException("GPT 응답의 'choices'가 비어 있습니다.");
        }

        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        Map<?, ?> messageMap = (Map<?, ?>) firstChoice.get("message");
        String content = (String) messageMap.get("content");

        return content;
    }
}
