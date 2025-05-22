package com.example.job_weather_back.weather.service;

import com.example.job_weather_back.config.GptProperties;
import com.example.job_weather_back.weather.dto.WeatherInputMetricsDto;
import com.example.job_weather_back.weather.dto.WeatherResponseDto; // DTO의 date 타입도 LocalDate여야 함
import com.example.job_weather_back.weather.dto.GptChatMessageDto;
import com.example.job_weather_back.weather.dto.GptChatRequestDto;
import com.example.job_weather_back.weather.dto.GptChatResponseDto;
import com.example.job_weather_back.weather.entity.JobWeather; // Entity의 date 타입도 LocalDate여야 함
import com.example.job_weather_back.weather.repository.JobWeatherRepository; // Repository의 PK 타입도 LocalDate여야 함

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate; // LocalDateTime 대신 LocalDate 사용
import java.time.LocalDateTime; // 스케줄러 로그용
import java.time.format.DateTimeFormatter; // 스케줄러 로그용
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final JobWeatherRepository jobWeatherRepository;
    private final RestTemplate restTemplate;
    private final GptProperties gptProperties;
    private final Random random = new Random();

    public WeatherService(JobWeatherRepository jobWeatherRepository,
                          RestTemplate restTemplate,
                          GptProperties gptProperties) {
        this.jobWeatherRepository = jobWeatherRepository;
        this.restTemplate = restTemplate;
        this.gptProperties = gptProperties;
    }

    @Transactional
    // 매일 9시, 12시, 18시 정각에 실행되도록 cron 표현식 변경
    @Scheduled(cron = "0 0 9,12,18 * * ?")
    public void scheduledGenerateAndSaveTodaysWeather() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.info("'오늘의 채용 날씨' 자동 생성 스케줄링 시작: {}", LocalDateTime.now().format(formatter));
        generateAndSaveTodaysWeatherInternal();
        log.info("'오늘의 채용 날씨' 자동 생성 스케줄링 완료: {}", LocalDateTime.now().format(formatter));
    }

    @Transactional
    public WeatherResponseDto generateAndSaveTodaysWeatherInternal() {
        LocalDate today = LocalDate.now(); // LocalDateTime -> LocalDate
        WeatherInputMetricsDto metrics = fetchJobMarketMetrics();
        int score = calculateScore(metrics);
        String weatherStatus = mapScoreToWeatherStatus(score);
        String prompt = generateGptPrompt(metrics, score, weatherStatus);
        String commentary = callGptApi(prompt);

        JobWeather weatherReport = JobWeather.builder()
                .date(today) // LocalDate 사용
                .score(score)
                .weather(weatherStatus)
                .commentary(commentary)
                .build();
        jobWeatherRepository.save(weatherReport);
        log.info("오늘의 채용 날씨 정보 저장/갱신 완료: {}", weatherReport);
        return convertToDto(weatherReport);
    }
    
    public WeatherResponseDto triggerTodaysWeatherGeneration() {
        log.info("수동으로 '오늘의 채용 날씨' 생성 요청.");
        return generateAndSaveTodaysWeatherInternal();
    }

    @Transactional(readOnly = true)
    public Optional<WeatherResponseDto> getLatestWeatherReport() {
        return jobWeatherRepository.findTopByOrderByDateDesc()
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Optional<WeatherResponseDto> getWeatherReportByDate(LocalDate date) { // 파라미터 LocalDate 유지
        // PK가 LocalDate이므로 findById 사용 가능
        return jobWeatherRepository.findById(date)
                .map(this::convertToDto);
    }

    // ... fetchJobMarketMetrics, calculateScore, mapScoreToWeatherStatus, generateGptPrompt, callGptApi는 이전과 동일 ...
    private WeatherInputMetricsDto fetchJobMarketMetrics() {
        log.info("채용 시장 지표 데이터 가져오는 중 (더미 데이터 시나리오 사용)");
        List<WeatherInputMetricsDto> scenarios = Arrays.asList(
            WeatherInputMetricsDto.builder().totalNewEntryLevelDeveloperJobsToday(120L).newJobsAbsoluteChangeFromYesterday(20).newJobsInMajorCompaniesToday(4).newJobsWithRemoteOptionToday(30).newInternshipToFullTimeJobsToday(10).trendingKeywords("AI 엔지니어, 데이터 과학자, 클라우드 네이티브").build(),
            WeatherInputMetricsDto.builder().totalNewEntryLevelDeveloperJobsToday(90L).newJobsAbsoluteChangeFromYesterday(10).newJobsInMajorCompaniesToday(3).newJobsWithRemoteOptionToday(20).newInternshipToFullTimeJobsToday(7).trendingKeywords("Spring Boot, React, Kubernetes, MSA").build(),
            WeatherInputMetricsDto.builder().totalNewEntryLevelDeveloperJobsToday(60L).newJobsAbsoluteChangeFromYesterday(0).newJobsInMajorCompaniesToday(1).newJobsWithRemoteOptionToday(10).newInternshipToFullTimeJobsToday(3).trendingKeywords("웹 개발, 풀스택, REST API").build(),
            WeatherInputMetricsDto.builder().totalNewEntryLevelDeveloperJobsToday(35L).newJobsAbsoluteChangeFromYesterday(-5).newJobsInMajorCompaniesToday(0).newJobsWithRemoteOptionToday(7).newInternshipToFullTimeJobsToday(2).trendingKeywords("경력직 우대, 채용 축소, 시장 관망").build(),
            WeatherInputMetricsDto.builder().totalNewEntryLevelDeveloperJobsToday(15L).newJobsAbsoluteChangeFromYesterday(-15).newJobsInMajorCompaniesToday(0).newJobsWithRemoteOptionToday(3).newInternshipToFullTimeJobsToday(0).trendingKeywords("채용 중단, 구조조정, 취업난 심화").build()
        );
        WeatherInputMetricsDto selectedScenario = scenarios.get(random.nextInt(scenarios.size()));
        log.info("선택된 더미 데이터 시나리오: {}", selectedScenario);
        return selectedScenario;
    }

    private int calculateScore(WeatherInputMetricsDto metrics) {
        if (metrics == null) { log.warn("입력 지표(metrics)가 null이므로 기본 점수 50점을 반환합니다."); return 50; }
        double score = 0;
        score += normalize(metrics.getTotalNewEntryLevelDeveloperJobsToday(), 0, 150, 40);
        score += normalizeChange(metrics.getNewJobsAbsoluteChangeFromYesterday(), -20, 20, 10, 20);
        if (metrics.getNewJobsInMajorCompaniesToday() >= 4) score += 25;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 3) score += 20;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 2) score += 15;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 1) score += 8;
        score += normalize(metrics.getNewJobsWithRemoteOptionToday(), 0, 30, 8);
        score += normalize(metrics.getNewInternshipToFullTimeJobsToday(), 0, 10, 7);
        int finalScore = Math.min(100, (int) Math.round(score));
        finalScore = Math.max(0, finalScore);
        log.info("채용 날씨 점수 계산 완료: {}점", finalScore);
        return finalScore;
    }

    private double normalize(long value, long minVal, long maxVal, double maxPoints) {
        if (value <= minVal) return 0; if (value >= maxVal) return maxPoints; if (maxVal == minVal) return (value >= minVal) ? maxPoints : 0; return ((double) (value - minVal) / (maxVal - minVal)) * maxPoints;
    }

    private double normalizeChange(int change, int minChange, int maxChange, double basePoints, double maxPoints) {
        if (change <= minChange) return 0; if (change >= maxChange) return maxPoints; if (change == 0) return basePoints; if (maxChange == 0 && minChange == 0) return basePoints; if (change > 0) { return (maxChange != 0) ? basePoints + ((double) change / maxChange) * (maxPoints - basePoints) : basePoints; } else { return (minChange != 0) ? Math.max(0, basePoints + ((double) change / Math.abs(minChange)) * basePoints) : basePoints; }
    }

    private String mapScoreToWeatherStatus(int score) {
        if (score >= 90) return "매우 맑음"; if (score >= 75) return "맑음"; if (score >= 60) return "구름 조금"; if (score >= 40) return "흐림"; return "비";
    }

    private String generateGptPrompt(WeatherInputMetricsDto metrics, int score, String weatherStatus) {
        String prompt = String.format( "오늘 신입 개발자 채용 시장은 전반적으로 '%s' 상태이며, 종합 점수는 %d점입니다. " + "오늘 %d개의 신규 신입 개발자 공고가 올라왔고, 어제 대비 %d개 변동된 수치입니다. " + "주요 IT 기업 중 %d곳에서 새로운 신입 공고를 시작했으며, " + "원격 근무가 가능한 신규 공고는 %d개, 정규직 전환형 인턴 공고는 %d개입니다. " + "최근 주요 언급 키워드는 '%s'입니다. " + "이 정보를 바탕으로 신입 구직자들에게 희망을 주면서도 현실적인 상황을 반영한 간결한 코멘트(100자 내외)를 작성해주세요. " + "코멘트는 부드럽고 친근한 어투로 부탁드립니다.", weatherStatus, score, metrics.getTotalNewEntryLevelDeveloperJobsToday(), metrics.getNewJobsAbsoluteChangeFromYesterday(), metrics.getNewJobsInMajorCompaniesToday(), metrics.getNewJobsWithRemoteOptionToday(), metrics.getNewInternshipToFullTimeJobsToday(), metrics.getTrendingKeywords() );
        log.info("생성된 GPT 프롬프트: {}", prompt); return prompt;
    }

    private String callGptApi(String prompt) {
        // ... (이전 GPT API 호출 로직과 동일) ...
        if (gptProperties == null) { log.error("GptProperties 객체가 주입되지 않았습니다 (null)."); return "설정 오류: GPT 속성 정보를 로드할 수 없습니다."; }
        if (gptProperties.getApiUrl() == null || gptProperties.getApiUrl().isEmpty()) { log.error("GPT API URL이 null이거나 비어있습니다. application.properties 설정을 확인해주세요."); return "설정 오류: GPT API URL이 설정되지 않았습니다."; }
        if (gptProperties.getApiKey() == null || gptProperties.getApiKey().isEmpty()) { log.error("GPT API Key가 null이거나 비어있습니다. application.properties 설정을 확인해주세요."); return "설정 오류: GPT API Key가 설정되지 않았습니다."; }
        log.info("GPT API 호출 시작. API URL: {}", gptProperties.getApiUrl());
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON); headers.setBearerAuth(gptProperties.getApiKey()); 
        GptChatMessageDto systemMessage = new GptChatMessageDto("system", "당신은 채용 시장 동향을 요약해주는 친절한 AI 어시스턴트입니다. 신입 개발자 구직자에게 희망적이면서도 현실적인 조언을 담아 코멘트를 작성합니다. 항상 한국어로 답변해주세요.");
        GptChatMessageDto userMessage = new GptChatMessageDto("user", prompt);
        List<GptChatMessageDto> messages = Arrays.asList(systemMessage, userMessage);
        GptChatRequestDto requestDto = GptChatRequestDto.builder() .model("gpt-3.5-turbo") .messages(messages) .max_tokens(250) .temperature(0.7) .build();
        HttpEntity<GptChatRequestDto> entity = new HttpEntity<>(requestDto, headers);
        try {
            ResponseEntity<GptChatResponseDto> response = restTemplate.postForEntity( gptProperties.getApiUrl(), entity, GptChatResponseDto.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getChoices() != null && !response.getBody().getChoices().isEmpty()) {
                String gptComment = response.getBody().getChoices().get(0).getMessage().getContent().trim();
                log.info("GPT API 응답 성공. 생성된 코멘트: {}", gptComment); return gptComment;
            } else {
                log.warn("GPT API로부터 유효한 응답을 받지 못했습니다. 상태 코드: {}, 응답 본문: {}", response.getStatusCode(), response.getBody());
                return "날씨 코멘트를 생성하는데 실패했습니다. (GPT 응답 오류)";
            }
        } catch (HttpClientErrorException e) {
            log.error("GPT API 호출 중 클라이언트 오류 발생 ({}): {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "날씨 코멘트를 생성하는 중 오류가 발생했습니다. (API 클라이언트 오류)";
        } catch (Exception e) {
            log.error("GPT API 호출 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
            return "날씨 코멘트를 생성하는 중 오류가 발생했습니다. (내부 서버 오류)";
        }
    }

    private WeatherResponseDto convertToDto(JobWeather weatherReport) {
        if (weatherReport == null) return null;
        return WeatherResponseDto.builder()
                .date(weatherReport.getDate()) // LocalDate 그대로 전달
                .score(weatherReport.getScore())
                .weather(weatherReport.getWeather())
                .commentary(weatherReport.getCommentary())
                .build();
    }
}
