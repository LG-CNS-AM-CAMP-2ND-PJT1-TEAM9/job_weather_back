package com.example.job_weather_back.weather.service; // 패키지명 weather.service로 가정

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random; // 랜덤 더미 데이터 선택용

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final JobWeatherRepository jobWeatherRepository;
    private final RestTemplate restTemplate;
    private final GptProperties gptProperties;
    private final Random random = new Random(); // 더미 데이터 랜덤 선택용

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
        WeatherInputMetricsDto metrics = fetchJobMarketMetrics(); // 더미 데이터 반환
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
        log.info("오늘의 채용 날씨 정보 저장 완료 (더미 데이터 기반): {}", weatherReport); // JobWeather에 toString() 구현 필요
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
        log.info("채용 시장 지표 데이터 가져오는 중 (더미 데이터 시나리오 사용)");

        List<WeatherInputMetricsDto> scenarios = Arrays.asList(
            WeatherInputMetricsDto.builder() // 매우 좋음 (예: 85~95점대 목표)
                .totalNewEntryLevelDeveloperJobsToday(120L) // 이전 150L -> 120L
                .newJobsAbsoluteChangeFromYesterday(20)    // 이전 30 -> 20
                .newJobsInMajorCompaniesToday(4)           // 이전 5 -> 4
                .newJobsWithRemoteOptionToday(30)          // 이전 40 -> 30
                .newInternshipToFullTimeJobsToday(10)      // 이전 15 -> 10
                .trendingKeywords("AI 엔지니어, 데이터 과학자, 클라우드 네이티브")
                .build(),
            WeatherInputMetricsDto.builder() // 좋음 (예: 70~80점대 목표)
                .totalNewEntryLevelDeveloperJobsToday(90L)
                .newJobsAbsoluteChangeFromYesterday(10)
                .newJobsInMajorCompaniesToday(3)
                .newJobsWithRemoteOptionToday(20)
                .newInternshipToFullTimeJobsToday(7)
                .trendingKeywords("Spring Boot, React, Kubernetes, MSA")
                .build(),
            WeatherInputMetricsDto.builder() // 보통 (예: 55~65점대 목표)
                .totalNewEntryLevelDeveloperJobsToday(60L)  // 이전 75L -> 60L
                .newJobsAbsoluteChangeFromYesterday(0)     // 이전 5 -> 0
                .newJobsInMajorCompaniesToday(1)           // 이전 2 -> 1
                .newJobsWithRemoteOptionToday(10)          // 이전 15 -> 10
                .newInternshipToFullTimeJobsToday(3)       // 이전 5 -> 3
                .trendingKeywords("웹 개발, 풀스택, REST API")
                .build(),
            WeatherInputMetricsDto.builder() // 약간 안좋음 (예: 40~50점대 목표)
                .totalNewEntryLevelDeveloperJobsToday(35L)
                .newJobsAbsoluteChangeFromYesterday(-5)
                .newJobsInMajorCompaniesToday(0)
                .newJobsWithRemoteOptionToday(7)
                .newInternshipToFullTimeJobsToday(2)
                .trendingKeywords("경력직 우대, 채용 축소, 시장 관망")
                .build(),
            WeatherInputMetricsDto.builder() // 매우 안좋음 (예: 20~35점대 목표)
                .totalNewEntryLevelDeveloperJobsToday(15L)  // 이전 20L -> 15L
                .newJobsAbsoluteChangeFromYesterday(-15)   // 이전 -10 -> -15
                .newJobsInMajorCompaniesToday(0)
                .newJobsWithRemoteOptionToday(3)           // 이전 5 -> 3
                .newInternshipToFullTimeJobsToday(0)       // 이전 1 -> 0
                .trendingKeywords("채용 중단, 구조조정, 취업난 심화")
                .build()
        );

        WeatherInputMetricsDto selectedScenario = scenarios.get(random.nextInt(scenarios.size()));
        log.info("선택된 더미 데이터 시나리오: {}", selectedScenario); // DTO에 @ToString 추가 권장
        return selectedScenario;
    }

    private int calculateScore(WeatherInputMetricsDto metrics) {
        if (metrics == null) {
            log.warn("입력 지표(metrics)가 null이므로 기본 점수 50점을 반환합니다.");
            return 50;
        }
        double score = 0;

        // 1. 오늘 등록된 전체 신입 개발자 공고 수 (최대 40점)
        // 기준: 0개=0점, 50개=15점, 100개=30점, 150개 이상=40점 (선형적이지 않게 조정 가능)
        score += normalize(metrics.getTotalNewEntryLevelDeveloperJobsToday(), 0, 150, 40);

        // 2. 어제 대비 신규 공고 수 변화량 (최대 20점)
        // 기준: -20개 이하=0점, 0개=10점(중간), +20개 이상=20점
        score += normalizeChange(metrics.getNewJobsAbsoluteChangeFromYesterday(), -20, 20, 10, 20);

        // 3. 주요 기업의 오늘 신규 신입 개발자 공고 수 (최대 25점)
        // 기준: 0개=0점, 1개=8점, 2개=15점, 3개=20점, 4개 이상=25점
        if (metrics.getNewJobsInMajorCompaniesToday() >= 4) score += 25;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 3) score += 20;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 2) score += 15;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 1) score += 8;

        // 4. 오늘 신규 공고 중 '원격/재택근무 가능' 옵션이 있는 공고 수 (최대 8점)
        // 기준: 0개=0점, 10개=3점, 20개=6점, 30개 이상=8점
        score += normalize(metrics.getNewJobsWithRemoteOptionToday(), 0, 30, 8);

        // 5. 오늘 신규 공고 중 '정규직 전환형 인턴' 공고 수 (최대 7점)
        // 기준: 0개=0점, 5개=3점, 10개 이상=7점
        score += normalize(metrics.getNewInternshipToFullTimeJobsToday(), 0, 10, 7);

        int finalScore = Math.min(100, (int) Math.round(score)); // 최종 점수는 0~100 사이
        finalScore = Math.max(0, finalScore); // 0점 미만 방지
        log.info("채용 날씨 점수 계산 완료: {}점", finalScore);
        return finalScore;
    }

    private double normalize(long value, long minVal, long maxVal, double maxPoints) {
        if (value <= minVal) return 0;
        if (value >= maxVal) return maxPoints;
        if (maxVal == minVal) return (value >= minVal) ? maxPoints : 0; // minVal과 maxVal이 같을 경우 처리
        return ((double) (value - minVal) / (maxVal - minVal)) * maxPoints;
    }

    private double normalizeChange(int change, int minChange, int maxChange, double basePoints, double maxPoints) {
        if (change <= minChange) return 0; // 최소 변화량 이하이면 0점
        if (change >= maxChange) return maxPoints; // 최대 변화량 이상이면 만점
        if (change == 0) return basePoints; // 변화 없으면 기본 점수

        // 0을 기준으로 양쪽으로 점수 배분
        if (maxChange == 0 && minChange == 0) return basePoints; // 변동폭 기준이 0이면 기본 점수

        if (change > 0) { // 증가 시
            // maxChange가 0이 아닐 때만 계산
            return (maxChange != 0) ? basePoints + ((double) change / maxChange) * (maxPoints - basePoints) : basePoints;
        } else { // 감소 시 (minChange는 음수일 수 있음)
            // minChange가 0이 아닐 때만 계산
            return (minChange != 0) ? Math.max(0, basePoints + ((double) change / Math.abs(minChange)) * basePoints) : basePoints;
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
        String prompt = String.format(
                "오늘 신입 개발자 채용 시장은 전반적으로 '%s' 상태이며, 종합 점수는 %d점입니다. " +
                "오늘 %d개의 신규 신입 개발자 공고가 올라왔고, 어제 대비 %d개 변동된 수치입니다. " +
                "주요 IT 기업 중 %d곳에서 새로운 신입 공고를 시작했으며, " +
                "원격 근무가 가능한 신규 공고는 %d개, 정규직 전환형 인턴 공고는 %d개입니다. " +
                "최근 주요 언급 키워드는 '%s'입니다. " +
                "이 정보를 바탕으로 신입 구직자들에게 희망을 주면서도 현실적인 상황을 반영한 간결한 코멘트(100자 내외)를 작성해주세요. " +
                "코멘트는 부드럽고 친근한 어투로 부탁드립니다.",
                weatherStatus, score,
                metrics.getTotalNewEntryLevelDeveloperJobsToday(),
                metrics.getNewJobsAbsoluteChangeFromYesterday(),
                metrics.getNewJobsInMajorCompaniesToday(),
                metrics.getNewJobsWithRemoteOptionToday(),
                metrics.getNewInternshipToFullTimeJobsToday(),
                metrics.getTrendingKeywords()
        );
        log.info("생성된 GPT 프롬프트: {}", prompt);
        return prompt;
    }

    private String callGptApi(String prompt) {
        if (gptProperties == null) {
            log.error("GptProperties 객체가 주입되지 않았습니다 (null).");
            return "설정 오류: GPT 속성 정보를 로드할 수 없습니다.";
        }
        if (gptProperties.getApiUrl() == null || gptProperties.getApiUrl().isEmpty()) {
            log.error("GPT API URL이 null이거나 비어있습니다. application.properties 설정을 확인해주세요.");
            return "설정 오류: GPT API URL이 설정되지 않았습니다.";
        }
        if (gptProperties.getApiKey() == null || gptProperties.getApiKey().isEmpty()) {
            log.error("GPT API Key가 null이거나 비어있습니다. application.properties 설정을 확인해주세요.");
            return "설정 오류: GPT API Key가 설정되지 않았습니다.";
        }

        log.info("GPT API 호출 시작. API URL: {}", gptProperties.getApiUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptProperties.getApiKey()); 

        GptChatMessageDto systemMessage = new GptChatMessageDto("system", "당신은 채용 시장 동향을 요약해주는 친절한 AI 어시스턴트입니다. 신입 개발자 구직자에게 희망적이면서도 현실적인 조언을 담아 코멘트를 작성합니다. 항상 한국어로 답변해주세요.");
        GptChatMessageDto userMessage = new GptChatMessageDto("user", prompt);
        
        List<GptChatMessageDto> messages = Arrays.asList(systemMessage, userMessage);

        GptChatRequestDto requestDto = GptChatRequestDto.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .max_tokens(250)
                .temperature(0.7)
                .build();

        HttpEntity<GptChatRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<GptChatResponseDto> response = restTemplate.postForEntity(
                    gptProperties.getApiUrl(),
                    entity,
                    GptChatResponseDto.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null &&
                response.getBody().getChoices() != null && !response.getBody().getChoices().isEmpty()) {
                String gptComment = response.getBody().getChoices().get(0).getMessage().getContent().trim();
                log.info("GPT API 응답 성공. 생성된 코멘트: {}", gptComment);
                return gptComment;
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
                .date(weatherReport.getDate())
                .score(weatherReport.getScore())
                .weather(weatherReport.getWeather())
                .commentary(weatherReport.getCommentary())
                .build();
    }
}
