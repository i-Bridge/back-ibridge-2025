package com.ibridge.service;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GptService {
    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();

    public String askGpt(String question) {
        try {
            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", "지금까지의 대화 내용은 다음과 같아:\n" + question +
                            "\n\n이 내용 중 마지막 아이의 답변을 바탕으로, 3살부터 초등 저학년까지의 어린아이가 재미있어할 만한 질문을 하나 만들어줘. " +
                            "아이의 눈높이에 맞춰 존댓말을 쓰지 말고, 간단하고 친근한 말투로 질문만 만들어줘. " +
                            "질문은 아이가 궁금해할 만한 포인트를 잘 잡고, 흥미를 끌 수 있도록 만들어줘.");

            JSONObject body = new JSONObject()
                    .put("model", "gpt-4-turbo")
                    .put("messages", List.of(message));

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("choices")) {
                        return jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();
                    } else {
                        return "OpenAI API 오류 응답: " + responseBody;
                    }
                }
            }
        } catch (Exception e) {
            return "GPT 응답 중 오류 발생: " + e.getMessage();
        }
        return "GPT 응답 없음";
    }
    public String summarizeGPT(String conversation) {
        try {
            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", "다음은 지금까지의 대화 내용이야:\n" + conversation +
                            "\n\n이 대화의 주제를 하나로 뽑아줘. " +
                            "주제는 핵심을 잘 나타내는 하나의 단어나 짧은 구절이어야 해. ");

            JSONObject body = new JSONObject()
                    .put("model", "gpt-4-turbo")
                    .put("messages", List.of(message));

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("choices")) {
                        return jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();
                    } else {
                        return "OpenAI API 오류 응답: " + responseBody;
                    }
                }
            }
        } catch (Exception e) {
            return "GPT 응답 중 오류 발생: " + e.getMessage();
        }
        return "GPT 응답 없음";
    }
}
