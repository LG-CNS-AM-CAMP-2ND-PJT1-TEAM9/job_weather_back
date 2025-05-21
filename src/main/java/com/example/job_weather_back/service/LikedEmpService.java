package com.example.job_weather_back.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LikedEmpService {
    
    // 채용 공고 id를 이용해 data 반환
    public Map<String, Object> getLikedEmpInfoByNum(int empNum) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "" + empNum; // 사람인 api

        // 더미 데이터로 테스트 - 테스트시 if부터 return까지 살리기
        // if (empNum >= 1 && empNum <= 10) {
        //     Map<String, Object> job = new HashMap<>();

        //     job.put("id", empNum);

        //     Map<String, Object> position = new HashMap<>();
        //     position.put("title", "백엔드 자바 개발자 " + empNum);

        //     Map<String, String> location = new HashMap<>();
        //     location.put("name", "서울특별시 강남구");

        //     Map<String, String> jobType = new HashMap<>();
        //     jobType.put("name", empNum % 2 == 0 ? "대기업SI" : "부트캠프");

        //     position.put("location", location);
        //     position.put("job-type", jobType);

        //     Map<String, String> company = new HashMap<>();
        //     company.put("name", "사람인 테스트 기업 " + empNum);

        //     job.put("position", position);
        //     job.put("company", company);
        //     job.put("expiration-date", "2025-06-" + String.format("%02d", empNum + 10));
        //     job.put("active", true);
        //     job.put("url", "https://www.saramin.co.kr/job/" + empNum);

        //     Map<String,Object> result = new HashMap<>();
        //     result.put("id", job.get("id")); // 공고번호
        //     result.put("title", position.get("title")); // 공고제목
        //     result.put("company", company.get("name")); // 회사명
        //     result.put("deadline", job.get("expiration-date")); // 마감일
        //     result.put("location", location.get("name")); // 근무지역
        //     result.put("active", job.get("active")); //공고 진행 여부
        //     result.put("url", job.get("url")); // url

        //     return result;
        // } else {
        //     return Collections.singletonMap("error", "유효하지 않은 공고 번호입니다.");
        // }

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();

            Map<String,Object> result = new HashMap<>();
            Map<String,Object> job = (Map<String,Object>) body.get("job");

            Map<String,Object> position = (Map<String,Object>) job.get("position");
            Map<String,Object> location = (Map<String,Object>) position.get("location");
            Map<String,Object> company = (Map<String,Object>) job.get("company");
            
            result.put("id", job.get("id")); // 공고번호
            result.put("title", position.get("title")); // 공고제목
            result.put("company", company.get("name")); // 회사명
            result.put("deadline", job.get("expiration-date")); // 마감일
            result.put("location", location.get("name")); // 근무지역
            result.put("active", job.get("active")); //공고 진행 여부
            result.put("url", job.get("url")); // url

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", "채용 정보를 가져오는 중 오류 발생");
        }
    }
}
