package com.example.job_weather_back.weather.service;

import com.example.job_weather_back.config.GptProperties;
import com.example.job_weather_back.weather.dto.WeatherInputMetricsDto;
import com.example.job_weather_back.weather.dto.WeatherResponseDto;
import com.example.job_weather_back.weather.dto.GptChatMessageDto;
import com.example.job_weather_back.weather.dto.GptChatRequestDto;
import com.example.job_weather_back.weather.dto.GptChatResponseDto;
import com.example.job_weather_back.weather.entity.JobWeather;
import com.example.job_weather_back.weather.repository.JobWeatherRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List; // List import
import java.util.Optional;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final JobWeatherRepository jobWeatherRepository;
    private final RestTemplate restTemplate;
    private final GptProperties gptProperties;

    public WeatherService(JobWeatherRepository jobWeatherRepository,
                          RestTemplate restTemplate,
                          GptProperties gptProperties) {
        this.jobWeatherRepository = jobWeatherRepository;
        this.restTemplate = restTemplate;
        this.gptProperties = gptProperties;
    }

    @Transactional
    public WeatherResponseDto generateAndSaveTodaysWeather() {
        LocalDate today = LocalDate.now();
        WeatherInputMetricsDto metrics = fetchJobMarketMetrics(); // 상세화된 DTO 반환 가정
        int score = calculateScore(metrics);
        String weatherStatus = mapScoreToWeatherStatus(score);
        String prompt = generateGptPrompt(metrics, score, weatherStatus); // 상세화된 프롬프트
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
        // TODO: 실제 채용 시장 지표 데이터를 가져오는 로직 구현 (사람인 API 등 연동 시)
        // 현재는 상세화된 DTO에 맞춰 임시 목업 데이터 반환
        return WeatherInputMetricsDto.builder()
                .totalNewEntryLevelDeveloperJobsToday(75L)      // 예: 오늘 신규 신입 개발자 공고 75개
                .newJobsAbsoluteChangeFromYesterday(10)         // 예: 어제보다 10개 증가
                .newJobsInMajorCompaniesToday(2)                // 예: 주요 기업에서 2개 공고
                .newJobsWithRemoteOptionToday(15)               // 예: 원격근무 가능 공고 15개
                .newInternshipToFullTimeJobsToday(5)            // 예: 정규직 전환형 인턴 5개
                .build();
    }

    private int calculateScore(WeatherInputMetricsDto metrics) {
        if (metrics == null) return 50; // 기본 점수

        double score = 0;

        // 1. 오늘 등록된 전체 신입 개발자 공고 수 (최대 40점)
        // 예: 0개 = 0점, 50개 = 20점, 100개 이상 = 40점
        score += normalize(metrics.getTotalNewEntryLevelDeveloperJobsToday(), 0, 100, 40);

        // 2. 어제 대비 신규 공고 수 변화량 (최대 20점)
        // 예: -20개 이하 = 0점, 0개 = 10점, +20개 이상 = 20점
        score += normalizeChange(metrics.getNewJobsAbsoluteChangeFromYesterday(), -20, 20, 10, 20);

        // 3. 주요 기업의 오늘 신규 신입 개발자 공고 수 (최대 25점)
        // 예: 0개 = 0점, 1개 = 10점, 2개 = 18점, 3개 이상 = 25점
        if (metrics.getNewJobsInMajorCompaniesToday() >= 3) {
            score += 25;
        } else if (metrics.getNewJobsInMajorCompaniesToday() == 2) {
            score += 18;
        } else if (metrics.getNewJobsInMajorCompaniesToday() == 1) {
            score += 10;
        }

        // 4. 오늘 신규 공고 중 '원격/재택근무 가능' 옵션이 있는 공고 수 (최대 7.5점 -> 반올림하여 8점 또는 7점으로 배분)
        // 예: 0개 = 0점, 10개 = 4점, 20개 이상 = 8점
        score += normalize(metrics.getNewJobsWithRemoteOptionToday(), 0, 20, 8);

        // 5. 오늘 신규 공고 중 '정규직 전환형 인턴' 공고 수 (최대 7.5점 -> 반올림하여 7점 또는 8점으로 배분)
        // 예: 0개 = 0점, 5개 = 4점, 10개 이상 = 7점
        score += normalize(metrics.getNewInternshipToFullTimeJobsToday(), 0, 10, 7);

        return Math.min(100, (int) Math.round(score)); // 0~100점 사이로 최종 점수 반환
    }

    // 값 정규화 헬퍼 메소드: 특정 범위의 값을 지정된 최대 점수 스케일로 변환
    private double normalize(long value, long minVal, long maxVal, double maxPoints) {
        if (value <= minVal) return 0;
        if (value >= maxVal) return maxPoints;
        return ((double) (value - minVal) / (maxVal - minVal)) * maxPoints;
    }

    // 변화량 정규화 헬퍼 메소드
    private double normalizeChange(int change, int minChange, int maxChange, double basePoints, double maxPoints) {
        if (change <= minChange) return 0;
        if (change >= maxChange) return maxPoints;
        // 0 변화(basePoints)를 기준으로 선형적으로 점수 배분
        if (change == 0) return basePoints;
        if (change > 0) { // 증가 시
            return basePoints + ((double) change / maxChange) * (maxPoints - basePoints);
        } else { // 감소 시
            return basePoints - ((double) Math.abs(change) / Math.abs(minChange)) * basePoints;
        }
    }


    private String mapScoreToWeatherStatus(int score) {
        if (score >= 90) return "매우 맑음";
        if (score >= 75) return "맑음";
        if (score >= 60) return "구름 조금";
        if (score >= 40) return "흐림";
        return "비";
    }

    private String generateGptPrompt(WeatherInputMetricsDto metrics, int score, String weatherStatus) {
        // 상세화된 DTO 필드를 활용하여 프롬프트 구성
        return String.format(
                "오늘 신입 개발자 채용 시장은 전반적으로 '%s' 상태이며, 종합 점수는 %d점입니다. " +
                "오늘 %d개의 신규 신입 개발자 공고가 올라왔고, 이는 어제 대비 %d개 변동된 수치입니다. " +
                "주요 IT 기업 중 %d곳에서 새로운 신입 공고를 시작했으며, " +
                "원격 근무가 가능한 신규 공고는 %d개, 정규직 전환형 인턴 공고는 %d개입니다. " +
                "이 정보를 바탕으로 신입 구직자들에게 희망을 주면서도 현실적인 상황을 반영한 간결한 코멘트(100자 내외)를 작성해주세요. " +
                "코멘트는 부드럽고 친근한 어투로 부탁드립니다.",
                weatherStatus, score,
                metrics.getTotalNewEntryLevelDeveloperJobsToday(),
                metrics.getNewJobsAbsoluteChangeFromYesterday(),
                metrics.getNewJobsInMajorCompaniesToday(),
                metrics.getNewJobsWithRemoteOptionToday(),
                metrics.getNewInternshipToFullTimeJobsToday()
        );
    }

    private String callGptApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptProperties.getApiKey());

        GptChatMessageDto systemMessage = new GptChatMessageDto("system", "당신은 채용 시장 동향을 요약해주는 친절한 AI 어시스턴트입니다. 신입 개발자 구직자에게 희망적이면서도 현실적인 조언을 담아 코멘트를 작성합니다.");
        GptChatMessageDto userMessage = new GptChatMessageDto("user", prompt);

        GptChatRequestDto requestDto = GptChatRequestDto.builder()
                .model("gpt-3.5-turbo") // 또는 gpt-4 등
                .messages(List.of(systemMessage, userMessage))
                .max_tokens(250) // 코멘트 길이에 맞춰 조정
                .temperature(0.7)
                .build();

        HttpEntity<GptChatRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<GptChatResponseDto> response = restTemplate.postForEntity(
                    gptProperties.getApiUrl(),
                    entity,
                    GptChatResponseDto.class);

            if (response.getBody() != null && response.getBody().getChoices() != null && !response.getBody().getChoices().isEmpty()) {
                return response.getBody().getChoices().get(0).getMessage().getContent().trim();
            }
            log.warn("GPT API로부터 유효한 응답을 받지 못했습니다. 응답: {}", response.getBody());
            return "날씨 코멘트를 생성하는데 실패했습니다. (GPT 응답 없음)";
        } catch (Exception e) {
            log.error("GPT API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "날씨 코멘트를 생성하는 중 오류가 발생했습니다. (API 호출 실패)";
        }
    }

    private WeatherResponseDto convertToDto(JobWeather weatherReport) {
        if (weatherReport == null) return null;
        return WeatherResponseDto.builder()
                .date(weatherReport.getDate())
                .score(weatherReport.getScore())
                .weather(weatherReport.getWeather())
                .commentary(weatherReport.getCommentary())
                .build();
    }
}
