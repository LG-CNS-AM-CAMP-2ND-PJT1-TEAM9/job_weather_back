package com.example.job_weather_back.controller;

import com.example.job_weather_back.service.NewsTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsTransferController {
    private final NewsTransferService newsTransferService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transferNews() {
        try {
            newsTransferService.transferNewsToNewContents();
            return ResponseEntity.ok("뉴스 데이터 이전이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("데이터 이전 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
