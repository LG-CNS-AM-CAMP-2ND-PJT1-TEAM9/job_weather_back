package com.example.job_weather_back.weather.controller; // 패키지명 weather.controller로 가정

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
import org.springframework.web.bind.annotation.CrossOrigin; // CORS 사용 시

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true") // 프론트엔드 주소에 맞게
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * (수동 트리거용) 오늘의 채용 날씨 정보를 생성하고 DB에 저장합니다.
     * @return 생성된 날씨 정보 또는 에러 메시지
     */
    @PostMapping("/today/generate-manual") // 경로를 명확히 구분
    public ResponseEntity<?> generateTodaysWeatherManually() {
        try {
            WeatherResponseDto responseDto = weatherService.triggerTodaysWeatherGeneration();
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("오늘의 날씨 정보 수동 생성 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<WeatherResponseDto> getLatestWeather() {
        Optional<WeatherResponseDto> weatherOpt = weatherService.getLatestWeatherReport();
        return weatherOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{date}")
    public ResponseEntity<WeatherResponseDto> getWeatherByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<WeatherResponseDto> weatherOpt = weatherService.getWeatherReportByDate(date);
        return weatherOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
