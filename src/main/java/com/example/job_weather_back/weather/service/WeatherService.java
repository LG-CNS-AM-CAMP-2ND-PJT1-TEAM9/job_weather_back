package com.example.job_weather_back.weather.service;

import com.example.job_weather_back.weather.dto.WeatherInputMetricsDto;
import com.example.job_weather_back.weather.dto.WeatherResponseDto;
import com.example.job_weather_back.weather.entity.JobWeather;
import com.example.job_weather_back.weather.repository.JobWeatherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;
// import org.slf4j.Logger; // 로깅 필요시
// import org.slf4j.LoggerFactory; // 로깅 필요시

@Service
public class WeatherService {

    // private static final Logger log = LoggerFactory.getLogger(WeatherService.class); // 로깅 필요시

    private final JobWeatherRepository jobWeatherRepository;
    private final RestTemplate restTemplate;

    @Value("${gpt.api.key}")
    private String gptApiKey;

    @Value("${gpt.api.url}")
    private String gptApiUrl;

    public WeatherService(JobWeatherRepository jobWeatherRepository, RestTemplate restTemplate) {
        this.jobWeatherRepository = jobWeatherRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public WeatherResponseDto generateAndSaveTodaysWeather() {
        LocalDate today = LocalDate.now();
        WeatherInputMetricsDto metrics = fetchJobMarketMetrics();
        int score = calculateScore(metrics);
        String weatherStatus = mapScoreToWeatherStatus(score);
        String prompt = generateGptPrompt(metrics, score, weatherStatus);
        String commentary = callGptApi(prompt);

        JobWeather weatherReport = JobWeather.builder()
                .date(today)
                .score(score)
                .weather(weatherStatus)
                .commentary(commentary)
                .build();
        jobWeatherRepository.save(weatherReport);
        return convertToDto(weatherReport);
    }

    @Transactional(readOnly = true)
    public Optional<WeatherResponseDto> getLatestWeatherReport() {
        return jobWeatherRepository.findTopByOrderByDateDesc()
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Optional<WeatherResponseDto> getWeatherReportByDate(LocalDate date) {
        return jobWeatherRepository.findById(date)
                .map(this::convertToDto);
    }

    private WeatherInputMetricsDto fetchJobMarketMetrics() {
        // TODO: 실제 채용 시장 지표 데이터를 가져오는 로직 구현
        // 임시 목업 데이터 반환 (WeatherInputMetricsDto 정의 후 파라미터 채우기)
        return new WeatherInputMetricsDto(/* ... */);
    }

    private int calculateScore(WeatherInputMetricsDto metrics) {
        // TODO: metrics를 기반으로 점수 계산 로직 구현
        if (metrics == null) return 50; // 임시 처리
        return 75; // 임시 고정값
    }

    private String mapScoreToWeatherStatus(int score) {
        // TODO: 점수에 따라 날씨 상태 매핑
        if (score >= 90) return "매우 맑음";
        if (score >= 75) return "맑음";
        if (score >= 60) return "구름 조금";
        if (score >= 40) return "흐림";
        return "비";
    }

    private String generateGptPrompt(WeatherInputMetricsDto metrics, int score, String weatherStatus) {
        // TODO: GPT에게 전달할 프롬프트 생성 로직 구현
        return String.format("오늘 채용 시장 날씨는 '%s'(%d점)입니다. 이와 관련하여 간단하고 긍정적인 코멘트를 100자 내외로 작성해주세요.", weatherStatus, score);
    }

    private String callGptApi(String prompt) {
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.setBearerAuth(gptApiKey);
        // Map<String, Object> requestBody = new HashMap<>();
        // ... (요청 본문 구성)
        // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // try {
        //     ResponseEntity<Map> response = restTemplate.postForEntity(gptApiUrl, entity, Map.class);
        //     // ... (응답 처리)
        // } catch (Exception e) {
        //     log.error("GPT API 호출 중 오류 발생: {}", e.getMessage());
        //     return "날씨 코멘트를 생성하는 중 오류가 발생했습니다.";
        // }
        return "GPT API 연동 예정: " + prompt; // 임시 반환값
    }

    private WeatherResponseDto convertToDto(JobWeather weatherReport) {
        if (weatherReport == null) return null;
        // WeatherResponseDto에 @Builder 추가 가정
        return WeatherResponseDto.builder()
                .date(weatherReport.getDate())
                .score(weatherReport.getScore())
                .weather(weatherReport.getWeather())
                .commentary(weatherReport.getCommentary())
                .build();
    }
}