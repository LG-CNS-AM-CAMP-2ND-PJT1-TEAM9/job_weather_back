package com.example.job_weather_back.weather.controller; // 패키지명 weather.controller로 가정

import com.example.job_weather_back.weather.dto.WeatherResponseDto;
import com.example.job_weather_back.weather.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag; // Tag import

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

@Tag(name = "Weather API", description = "'오늘의 채용 날씨' 관련 API")
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
    @Operation(summary = "오늘의 채용 날씨 정보 수동 생성", description = "스케줄러와 별개로 현재 시점의 날씨 정보를 강제로 생성하고 DB에 저장합니다. (주로 테스트용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "날씨 정보 생성 및 저장 성공",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = WeatherResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생", content = @Content)
    })
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

    @Operation(summary = "최신 채용 날씨 정보 조회", description = "DB에 저장된 가장 최근 날짜의 채용 날씨 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = WeatherResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "데이터 없음", content = @Content)
    })
    @GetMapping("/latest")
    public ResponseEntity<WeatherResponseDto> getLatestWeather() {
        Optional<WeatherResponseDto> weatherOpt = weatherService.getLatestWeatherReport();
        return weatherOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "특정 날짜 채용 날씨 정보 조회", description = "URL 경로의 {date}에 해당하는 날짜의 채용 날씨 정보를 DB에서 조회하여 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = WeatherResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "해당 날짜 데이터 없음", content = @Content)
    })
    @GetMapping("/{date}")
    public ResponseEntity<WeatherResponseDto> getWeatherByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<WeatherResponseDto> weatherOpt = weatherService.getWeatherReportByDate(date);
        return weatherOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
