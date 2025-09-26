package com.ibridge.service;

import com.ibridge.domain.entity.Subject;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
            System.out.println("gpt 응답 시작 : " + LocalDateTime.now());

            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", "대화 내용: \"" + question + "\"\n후속 질문 한 개만 만들어줘.");


            JSONObject systemMessage = new JSONObject()
                    .put("role", "system")
                    .put("content", """
                        너는 4~8세 아이와 대화하는 친근한 선생님이야.
                        아이가 말한 내용에 대해 공감하는 말 하나와 후속 질문 하나만 만들어야 해.
                        공감과 질문 외에는 어떤 말도 하지 마.
                        항상 친근하고 간단하게 질문을 만들어.
                        """);
            JSONObject body = new JSONObject()
                    .put("model", "gpt-5-mini")
                    .put("messages", List.of(systemMessage, message));

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
                        System.out.println("gpt 응답 종료 : " + LocalDateTime.now());
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
                    .put("model", "gpt-5-mini")
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
                    .put("model", "gpt-5-mini")
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

    public Map<String, Integer> positiveGPT(String conversation){
        String prompt = """
        다음 대화 내용을 분석해서 긍정/부정 비율을 퍼센트로 계산하고,
        출력은 반드시 JSON 형식으로 해주세요.
        예: {"긍정": 85, "부정": 15}

        [대화 내용 시작]
        %s
        [대화 내용 끝]
        """.formatted(conversation);

        try {
            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", prompt);

            JSONObject body = new JSONObject()
                    .put("model", "gpt-5-mini")
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
                        String content = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();

                        // Map<String, Integer>로 변환
                        JSONObject resultJson = new JSONObject(content);
                        Map<String, Integer> resultMap = new HashMap<>();
                        Iterator<String> keys = resultJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            resultMap.put(key, resultJson.getInt(key));
                        }

                        return resultMap;
                    } else {
                        throw new RuntimeException("OpenAI API 오류 응답: " + responseBody);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 중 오류 발생: " + e.getMessage(), e);
        }
        return Collections.emptyMap();
    }

    public Map<String, List<Long>> categorizeSubjects(List<Subject> subjects) {
        // Subject title만 추출해서 GPT에게 전달
        Map<String, Long> titleToIdMap = new HashMap<>();
        List<String> titles = new ArrayList<>();
        for (Subject subject : subjects) {
            titles.add(subject.getTitle());
            titleToIdMap.put(subject.getTitle(), subject.getId());
        }

        String prompt = """
        다음 주제들을 의미적으로 비슷한 것끼리 카테고리별로 묶어주세요.
        카테고리 이름은 GPT가 자유롭게 생성하되, 반드시 JSON 형식으로만 출력해야 합니다.
        다른 설명, 해설, 문장은 절대 포함하지 마세요.
        
        출력 예시:
        {
          "가족 활동": ["아이가 아빠와 쇼핑을 갔던 대화", "아이가 엄마와 놀이공원을 갔던 대화"],
          "학교 활동": ["아이가 친구와 학교에서 놀던 대화"]
        }
        
        [주제 목록 시작]
        %s
        [주제 목록 끝]
        """.formatted(String.join(", ", titles));

        try {
            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", prompt);

            JSONObject body = new JSONObject()
                    .put("model", "gpt-5-mini")
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
                        String content = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();

                        // JSON 파싱
                        JSONObject resultJson = new JSONObject(content);
                        Map<String, List<Long>> resultMap = new HashMap<>();

                        Iterator<String> keys = resultJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            List<Long> values = new ArrayList<>();

                            JSONArray arr = resultJson.getJSONArray(key);
                            for (int i = 0; i < arr.length(); i++) {
                                String subjectTitle = arr.getString(i);
                                Long subjectId = titleToIdMap.get(subjectTitle);
                                if (subjectId != null) {
                                    values.add(subjectId);
                                }
                            }

                            resultMap.put(key, values);
                        }

                        return resultMap;
                    } else {
                        throw new RuntimeException("OpenAI API 오류 응답: " + responseBody);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 중 오류 발생: " + e.getMessage(), e);
        }
        return Collections.emptyMap();
    }


}
