package com.ibridge.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class TokenValidationInterceptor implements HandlerInterceptor {

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${NAVER_CLIENT_ID}")
    private String naverClientId;
    @Value("${NAVER_CLIENT_SECRET}")
    private String naverSecretId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token is missing.");
            return false;
        }

        String accessToken = authHeader.substring(7);
        String provider = request.getHeader("Provider");  // 구글/네이버를 구분하는 헤더

        try {
            if ("google".equalsIgnoreCase(provider)) {
                return validateGoogleToken(request, response, accessToken);
            } else if ("naver".equalsIgnoreCase(provider)) {
                return validateNaverToken(request, response, accessToken);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid provider.");
                return false;
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired access token.");
            return false;
        }
    }

    private boolean validateGoogleToken(HttpServletRequest request, HttpServletResponse response, String accessToken) throws IOException {
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);

        Map<String, Object> body = userInfoResponse.getBody();
        String email = (String) body.get("email");
        if (email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("유효하지 않은 access token입니다.");
            return false;
        }
        String name = (String) body.get("name");

        request.setAttribute("email", email);
        request.setAttribute("name", name);
        return true;
    }

    private boolean validateNaverToken(HttpServletRequest request, HttpServletResponse response, String accessToken) throws IOException {
        String url = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverSecretId);

        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);

        Map<String, Object> body = userInfoResponse.getBody();
        Map<String, Object> responseBody = (Map<String, Object>) body.get("response");
        String email = (String) responseBody.get("email");
        if (email == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("유효하지 않은 access token입니다.");
            return false;
        }
        String name = (String) responseBody.get("name");

        request.setAttribute("email", email);
        request.setAttribute("name", name);
        return true;
    }
}

