package com.example.job_weather_back.controller; // 또는 com.example.job_weather_back.recommendation.controller;

import com.example.job_weather_back.dto.MainPageRecommendationsDto;
import com.example.job_weather_back.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/main/recommendations")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
    private final RecommendationService recommendationService;

    @Autowired // 생성자가 하나만 있을 경우 Spring 5부터는 생략 가능
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<MainPageRecommendationsDto> getMainPageRecommendations() {
        log.info("메인 페이지 추천 정보 요청 수신.");
        try {
            // 메인 페이지 접근 시마다 일반 사용자 추천 목록 업데이트
            // 뉴스 수집은 RecommendationService 내부의 스케줄러 또는 이 메소드에서 호출된
            // fetchAndSaveNewsFromNaverApi가 담당 (현재는 스케줄러 제거 상태)
            // fetchAndSaveNewsFromNaverApi를 여기서 호출하면 매번 API를 호출하므로 주의
            // recommendationService.fetchAndSaveNewsFromNaverApi(); // 필요시 주석 해제 (매우 주의)
            recommendationService.populateGeneralRecommendations();
            log.info("일반 사용자 추천 목록 업데이트 완료.");

        } catch (Exception e) {
            log.error("추천 콘텐츠 사전 작업 중 오류 발생", e);
            // 이 경우, 그냥 기존에 저장된 추천을 보여주거나 에러를 반환할 수 있습니다.
            // 여기서는 일단 getRecommendationsForMainPage()를 계속 진행합니다.
        }

        MainPageRecommendationsDto recommendations = recommendationService.getRecommendationsForMainPage();
        if (recommendations.getNews() == null || recommendations.getNews().isEmpty() /* && (recommendations.getJobs() == null || recommendations.getJobs().isEmpty()) */) {
            // 추천할 내용이 전혀 없을 경우 204 No Content
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recommendations);
    }

    // 스케줄러 대신 수동으로 뉴스 수집 및 추천 생성을 트리거하기 위한 엔드포인트 (개발/테스트용)
    @PostMapping("/populate-general")
    public ResponseEntity<String> populateGeneralRecommendationsManually() {
        try {
            log.info("수동으로 뉴스 수집 및 일반 사용자 추천 콘텐츠 생성 시작...");
            recommendationService.fetchAndSaveNewsFromNaverApi(); // 뉴스 먼저 수집
            recommendationService.populateGeneralRecommendations(); // 그 다음 추천 생성
            return ResponseEntity.ok("뉴스 수집 및 일반 사용자 추천 콘텐츠 생성을 완료했습니다.");
        } catch (Exception e) {
            log.error("수동 추천 콘텐츠 생성 중 오류 발생", e);
            return ResponseEntity.status(500).body("추천 콘텐츠 생성 중 오류 발생: " + e.getMessage());
        }
    }
}
