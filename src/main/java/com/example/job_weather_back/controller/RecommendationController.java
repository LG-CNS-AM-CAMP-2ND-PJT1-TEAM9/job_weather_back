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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Tag(name = "Main Page API", description = "메인 페이지 추천 및 날씨 관련 API")
@RestController
@RequestMapping("/api/main/recommendations")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class RecommendationController {

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
    public ResponseEntity<MainPageRecommendationsDto> getMainPageRecommendations(@Parameter(hidden = true) HttpSession session) {
        log.info("메인 페이지 추천 정보 요청 수신.");
        MainPageRecommendationsDto recommendations = recommendationService.getRecommendationsForMainPage(session);
        
        if (recommendations == null || (recommendations.getNews() == null || recommendations.getNews().isEmpty())) {
            log.info("반환할 추천 뉴스가 없습니다.");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(recommendations);
    }

    @Operation(summary = "일반 사용자용 추천 콘텐츠 수동 생성", description = "스케줄러와 별개로, 현재 DB에 저장된 뉴스를 기반으로 일반 사용자용 추천 콘텐츠를 강제로 생성하고 `user_recommendations` 테이블에 저장합니다. (주로 테스트용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일반 사용자 추천 생성 완료", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"일반 사용자 추천 콘텐츠 생성을 완료했습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생", content = @Content)
    })
    @PostMapping("/populate-general")
    public ResponseEntity<Map<String, String>> populateGeneralRecommendationsManually() {
        try {
            log.info("수동으로 일반 사용자 추천 콘텐츠 생성 시작...");
            // 뉴스 수집은 NewsController의 스케줄러가 담당하므로 여기서는 호출하지 않음.
            // recommendationService.fetchAndSaveNewsFromNaverApi(); 
            recommendationService.populateGeneralRecommendations();
            log.info("일반 사용자 추천 콘텐츠 생성 완료.");
            return ResponseEntity.ok(Map.of("message", "일반 사용자 추천 콘텐츠 생성을 완료했습니다."));
        } catch (Exception e) {
            log.error("수동 일반 사용자 추천 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "추천 콘텐츠 생성 중 오류 발생: " + e.getMessage()));
        }
    }

    // 특정 사용자의 추천을 수동으로 생성하는 API 활성화
    @Operation(summary = "특정 사용자 맞춤 추천 콘텐츠 수동 생성", description = "특정 사용자의 맞춤 추천 콘텐츠를 강제로 생성하고 `user_recommendations` 테이블에 저장합니다. (주로 테스트용, 로그인 기능 구현 후 사용)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 맞춤 추천 생성 완료", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"사용자 1의 맞춤 추천 콘텐츠 생성을 완료했습니다.\"}"))),
        @ApiResponse(responseCode = "400", description = "잘못된 사용자 ID (예: null 또는 형식 오류)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"사용자 ID가 필요합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생", content = @Content)
    })
    @PostMapping("/populate-user/{userSn}")
    public ResponseEntity<Map<String, String>> populateUserSpecificRecommendationsManually(
            @Parameter(description = "추천을 생성할 사용자의 고유 번호(userSn)", required = true, example = "1")
            @PathVariable Integer userSn) { // User 엔티티의 userSn 타입이 int이므로 Integer로 받음
        if (userSn == null) {
            log.warn("사용자 맞춤 추천 생성 요청: userSn이 null입니다.");
            return ResponseEntity.badRequest().body(Map.of("message", "사용자 ID가 필요합니다."));
        }
        try {
            log.info("{} 사용자의 맞춤 추천 콘텐츠 수동 생성 시작...", userSn);
            recommendationService.populateUserSpecificRecommendations(userSn);
            log.info("{} 사용자의 맞춤 추천 콘텐츠 생성 완료.", userSn);
            return ResponseEntity.ok(Map.of("message", userSn + " 사용자의 맞춤 추천 콘텐츠 생성을 완료했습니다."));
        } catch (Exception e) {
            log.error("{} 사용자 맞춤 추천 생성 중 오류 발생", userSn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "맞춤 추천 생성 중 오류: " + e.getMessage()));
        }
    }
}
