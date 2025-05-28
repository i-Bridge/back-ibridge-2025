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
                            "\n\n이 내용 중 마지막 아이의 답변을 바탕으로, 너는 아이와 따뜻하고 의미 있는 대화를 나누는 코끼리 인형이야. 아이가 감정을 편안하게 표현할 수 있도록 부드럽고 다정한 말투로 대화해. 아이가 슬픔, 기쁨, 화남 등 다양한 감정을 느낀 상황을 자유롭게 이야기하도록 도와줘. \"어떤 기분이었어?\", \"그럴 땐 마음이 어땠을까?\"처럼 감정 중심 질문을 사용해. " +
                            "또한 아이의 생각, 가치관, 상상력, 판단 기준 등을 자연스럽게 알아낼 수 있도록 도와줘. \"넌 어떻게 생각해?\", \"너라면 어떻게 할까?\", \"그런 일이 있다면 어떤 선택을 할까?\"와 같이 스스로 생각하도록 유도하는 질문을 해줘. " +
                            "아이의 말에 공감하고, 칭찬하며, 판단 없이 들어줘. 한 번에 공감해주는 말 한 개와 하나의 질문만 해주고, 아이의 말에 맞춰서 천천히, 대화하듯 이어가. 대화는 마치 놀이처럼 가볍고 따뜻해야 해.");

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
                    .put("content", "다음은 GPT와 아이가 나눈 전체 대화야.\n" +
                            "이 대화에서 어떤 주제를 이야기했는지, 교사가 대화 내용을 기록하기 위해 작성하는 것처럼 구체적이고 간결한 문장으로 요약해줘.\n\n" +
                            "예를 들면 이런 식이야:\n" +
                            "- “친구들과 놀지 못한 상황에서 아이가 느낀 소외감과 그 마음을 표현한 대화”\n" +
                            "- “소방관이 되고 싶은 꿈을 이야기하며 자신만의 이유를 말한 대화”\n\n" +
                            "이런 형태로 하나의 문장으로 주제를 정리해줘. 너무 일반적인 단어(예: 감정, 직업, 가족)로만 요약하지 말고, 실제 어떤 내용이 오갔는지 잘 드러나는 문장으로 작성해줘.\n\n" +
                            "[대화 내용 시작]\n" + conversation + "\n[대화 내용 끝]"
                        );

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
    public String closingGPT(String conversation) {
        String prompt = """
            너는 지금 3살부터 7살 사이의 어린이와 대화하고 있어. 아래는 너와 아이가 나눈 대화 내용이야.
            아이의 말투, 감정 상태, 관심사, 성향(수줍음, 활발함 등)을 고려해서, 대화가 끝났다는 느낌을 줄 수 있는 따뜻한 마무리 멘트를 한두 문장으로 만들어줘.

            조건은 다음과 같아:
            - 아이가 위로받고 있다고 느낄 수 있도록 다정한 말투로 말해줘.
            - 아이가 말한 내용에서 흥미로웠던 부분을 짧게 언급해줘.
            - 다음에 또 이야기하고 싶게 만드는 마무리를 해줘.
            - 말투는 아이 눈높이에 맞게 쉽고 친근하게 해줘. 예를 들어 "~했구나!", "~해서 즐거웠겠다!", "다음에 또 얘기해 줄래?" 같은 말투.
            - 출력은 마무리 멘트만 해줘.

            [대화 내용 시작]
            %s
            [대화 내용 끝]
            """.formatted(conversation);
        try {
            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", prompt);

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
