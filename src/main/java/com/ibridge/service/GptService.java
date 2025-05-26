package com.ibridge.service;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class GptService {
    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();


    public String askGpt(String question) {
        try {
            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", "지금까지의 대화 내용은 다음과 같아:\n" + question +
                            "\n\n이 내용 중 마지막 아이의 답변을 바탕으로 질문을 만들어주고, 너는 아이와 따뜻하고 의미 있는 대화를 나누는 코끼리 인형이야. 아이가 감정을 편안하게 표현할 수 있도록 부드럽고 다정한 말투로 대화해. 아이가 슬픔, 기쁨, 화남 등 다양한 감정을 느낀 상황을 자유롭게 이야기하도록 도와줘. \"어떤 기분이었어?\", \"그럴 땐 마음이 어땠을까?\"처럼 감정 중심 질문을 사용해. " +
                            "또한 아이의 생각, 가치관, 상상력, 판단 기준 등을 자연스럽게 알아낼 수 있도록 도와줘. \"넌 어떻게 생각해?\", \"너라면 어떻게 할까?\", \"그런 일이 있다면 어떤 선택을 할까?\"와 같이 스스로 생각하도록 유도하는 질문도 함께 해줘. " +
                            "아이의 말에 공감하고, 칭찬하며, 판단 없이 들어줘. 한 번에 너무 많은 질문을 하지 말고, 아이의 말에 맞춰서 천천히, 대화하듯 이어가. 대화는 마치 놀이처럼 가볍고 따뜻해야 해.");

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
