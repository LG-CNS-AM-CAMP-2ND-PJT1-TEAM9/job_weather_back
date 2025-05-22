package com.example.job_weather_back.controller; // 실제 컨트롤러 패키지 경로

import com.example.job_weather_back.dto.MainPageRecommendationsDto;
import com.example.job_weather_back.service.RecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession; // HttpSession import 추가

import org.slf4j.Logger; // Logger import 추가
import org.slf4j.LoggerFactory; // LoggerFactory import 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

// 만약 이 컨트롤러가 RecommendationController라면 클래스명도 변경하는 것이 좋습니다.
// 여기서는 파일명은 WeatherController.java로 되어있지만, 내용은 RecommendationController에 가깝습니다.
// @Tag(name = "Recommendation API", description = "메인 페이지 추천 관련 API")
@Tag(name = "Main Page API", description = "메인 페이지 추천 및 날씨 관련 API") // 태그명 수정
@RestController
@RequestMapping("/api/main/recommendations") // 이 컨트롤러는 추천 관련이므로 경로 유지
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class RecommendationController { // 클래스명도 RecommendationController로 변경하는 것이 좋음

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "메인 페이지 추천 콘텐츠 조회", description = "로그인 상태에 따라 맞춤형 또는 일반 추천 뉴스/채용공고 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = MainPageRecommendationsDto.class)) }),
            @ApiResponse(responseCode = "204", description = "추천할 콘텐츠 없음", content = @Content)
    })
    @GetMapping
    public ResponseEntity<MainPageRecommendationsDto> getMainPageRecommendations(HttpSession session) { // HttpSession 파라미터 추가
        log.info("메인 페이지 추천 정보 요청 수신.");
        try {
            // 서비스 메소드에서 필요시 뉴스 수집 및 추천 생성을 먼저 수행하도록 변경했으므로,
            // 컨트롤러에서는 getRecommendationsForMainPage만 호출합니다.
            // recommendationService.populateGeneralRecommendations(); // 이전에 여기서 호출하던 로직은 서비스 내부 또는 스케줄러로 이동
        } catch (Exception e) {
            log.error("추천 콘텐츠 사전 작업 중 오류 발생 (컨트롤러)", e);
            // 서비스에서 오류를 처리하고, 여기서는 최종 조회만 시도
        }

        MainPageRecommendationsDto recommendations = recommendationService.getRecommendationsForMainPage(session); // session 객체 전달
        if (recommendations == null || (recommendations.getNews() == null || recommendations.getNews().isEmpty()) /* && (recommendations.getJobs() == null || recommendations.getJobs().isEmpty()) */) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recommendations);
    }

    @Operation(summary = "일반 사용자용 추천 콘텐츠 수동 생성", description = "스케줄러와 별개로 현재 시점의 뉴스 수집 및 일반 사용자용 추천 콘텐츠를 강제로 생성하고 DB에 저장합니다. (주로 테스트용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘텐츠 생성 작업 시작/완료", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생", content = @Content)
    })
    @PostMapping("/populate-general")
    public ResponseEntity<String> populateGeneralRecommendationsManually() {
        try {
            log.info("수동으로 뉴스 수집 및 일반 사용자 추천 콘텐츠 생성 시작...");
            recommendationService.fetchAndSaveNewsFromNaverApi();
            recommendationService.populateGeneralRecommendations();
            return ResponseEntity.ok("뉴스 수집 및 일반 사용자 추천 콘텐츠 생성을 완료했습니다.");
        } catch (Exception e) {
            log.error("수동 추천 콘텐츠 생성 중 오류 발생", e);
            return ResponseEntity.status(500).body("추천 콘텐츠 생성 중 오류 발생: " + e.getMessage());
        }
    }

    // 만약 특정 사용자의 추천을 수동으로 생성하는 API가 필요하다면 추가
    /*
    @Operation(summary = "특정 사용자 맞춤 추천 콘텐츠 수동 생성", description = "특정 사용자의 맞춤 추천 콘텐츠를 강제로 생성합니다. (테스트용)")
    @PostMapping("/populate-user/{userSn}")
    public ResponseEntity<String> populateUserSpecificRecommendationsManually(@PathVariable Integer userSn) {
        try {
            log.info("{} 사용자의 맞춤 추천 콘텐츠 수동 생성 시작...", userSn);
            recommendationService.populateUserSpecificRecommendations(userSn);
            return ResponseEntity.ok(userSn + " 사용자의 맞춤 추천 콘텐츠 생성을 완료했습니다.");
        } catch (Exception e) {
            log.error("{} 사용자 맞춤 추천 생성 중 오류 발생", userSn, e);
            return ResponseEntity.status(500).body("맞춤 추천 생성 중 오류: " + e.getMessage());
        }
    }
    */
}
