package com.example.job_weather_back.weather.controller;

import com.example.job_weather_back.weather.dto.WeatherResponseDto;
import com.example.job_weather_back.weather.service.WeatherService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/weather") // API 기본 경로 설정
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * 오늘의 채용 날씨 정보를 생성하고 DB에 저장합니다.
     * (보통 스케줄러가 담당하지만, 테스트를 위해 수동 트리거 엔드포인트를 만듭니다)
     * @return 생성된 날씨 정보 또는 에러 메시지
     */
    @PostMapping("/today/generate")
    public ResponseEntity<?> generateTodaysWeather() {
        try {
            WeatherResponseDto responseDto = weatherService.generateAndSaveTodaysWeather();
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            // 실제로는 좀 더 구체적인 예외 처리와 로깅이 필요합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("오늘의 날씨 정보 생성 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 가장 최근에 생성된 채용 날씨 정보를 조회합니다.
     * @return 최신 날씨 정보 또는 404 Not Found
     */
    @GetMapping("/latest")
    public ResponseEntity<WeatherResponseDto> getLatestWeather() {
        Optional<WeatherResponseDto> weatherOpt = weatherService.getLatestWeatherReport();
        return weatherOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 특정 날짜의 채용 날씨 정보를 조회합니다.
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 해당 날짜의 날씨 정보 또는 404 Not Found
     */
    @GetMapping("/{date}")
    public ResponseEntity<WeatherResponseDto> getWeatherByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<WeatherResponseDto> weatherOpt = weatherService.getWeatherReportByDate(date);
        return weatherOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
