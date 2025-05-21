package com.example.job_weather_back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.job_weather_back.dto.NaverDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NaverService {
    @Value("${naver.client_id}")
    private String naver_client_id;

    @Value("${naver.redirect_uri}")
    private String naver_redirect_uri;

    @Value("${naver.client_secret}")
    private String naver_client_secret;

    public String getAccessToken(String code) {
        try {
            String requestUrl = "https://nid.naver.com/oauth2.0/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", naver_client_id);
            params.add("redirect_uri", naver_redirect_uri);
            params.add("client_secret", naver_client_secret);

            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("access_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("네이버 토큰 요청 실패", e);
        }
    }

    public NaverDto getUserInfo(String accessToken) {
        try {
            String requestUrl = "https://openapi.naver.com/v1/nid/me";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    entity,
                    String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode responseNode = root.path("response");

            Long naverId = responseNode.path("id").asLong();
            String email = responseNode.path("email").asText(null);
            String nickname = responseNode.path("nickname").asText(null);
            String userPhone = responseNode.path("mobile").asText(null);

            return new NaverDto(naverId, email, nickname, userPhone);

        } catch (Exception e) {
            throw new RuntimeException("네이버 사용자 정보 요청 실패", e);
        }
    }

}
