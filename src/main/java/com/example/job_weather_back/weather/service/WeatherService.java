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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays; // Java 8 이하에서 List.of 대신 사용
import java.util.List;
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
        WeatherInputMetricsDto metrics = fetchJobMarketMetrics();
        int score = calculateScore(metrics);
        String weatherStatus = mapScoreToWeatherStatus(score);
        String prompt = generateGptPrompt(metrics, score, weatherStatus);
        String commentary = callGptApi(prompt); // GPT API 실제 호출

        JobWeather weatherReport = JobWeather.builder()
                .date(today)
                .score(score)
                .weather(weatherStatus)
                .commentary(commentary)
                .build();
        jobWeatherRepository.save(weatherReport);
        log.info("오늘의 채용 날씨 정보 저장 완료: {}", weatherReport);
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
        log.info("채용 시장 지표 데이터 가져오는 중 (현재는 목업 데이터 사용)");
        return WeatherInputMetricsDto.builder()
                .totalNewEntryLevelDeveloperJobsToday(75L)
                .newJobsAbsoluteChangeFromYesterday(10)
                .newJobsInMajorCompaniesToday(2)
                .newJobsWithRemoteOptionToday(15)
                .newInternshipToFullTimeJobsToday(5)
                .build();
    }

    private int calculateScore(WeatherInputMetricsDto metrics) {
        if (metrics == null) {
            log.warn("입력 지표(metrics)가 null이므로 기본 점수 50점을 반환합니다.");
            return 50;
        }

        double score = 0;
        score += normalize(metrics.getTotalNewEntryLevelDeveloperJobsToday(), 0, 100, 40);
        score += normalizeChange(metrics.getNewJobsAbsoluteChangeFromYesterday(), -20, 20, 10, 20);
        if (metrics.getNewJobsInMajorCompaniesToday() >= 3) score += 25;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 2) score += 18;
        else if (metrics.getNewJobsInMajorCompaniesToday() == 1) score += 10;
        score += normalize(metrics.getNewJobsWithRemoteOptionToday(), 0, 20, 8);
        score += normalize(metrics.getNewInternshipToFullTimeJobsToday(), 0, 10, 7);

        int finalScore = Math.min(100, (int) Math.round(score));
        log.info("채용 날씨 점수 계산 완료: {}점, 입력 지표: {}", finalScore, metrics);
        return finalScore;
    }

    private double normalize(long value, long minVal, long maxVal, double maxPoints) {
        if (value <= minVal) return 0;
        if (value >= maxVal) return maxPoints;
        return ((double) (value - minVal) / (maxVal - minVal)) * maxPoints;
    }

    private double normalizeChange(int change, int minChange, int maxChange, double basePoints, double maxPoints) {
        if (change <= minChange) return 0;
        if (change >= maxChange) return maxPoints;
        if (change == 0) return basePoints;
        if (change > 0) return basePoints + ((double) change / maxChange) * (maxPoints - basePoints);
        return basePoints - ((double) Math.abs(change) / Math.abs(minChange)) * basePoints;
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
        log.info("생성된 GPT 프롬프트: {}", prompt);
        return prompt;
    }

    private String callGptApi(String prompt) {
                if (gptProperties == null) {
            log.error("GptProperties 객체가 주입되지 않았습니다 (null).");
            return "설정 오류: GPT 속성 정보를 로드할 수 없습니다.";
        }
        log.info("GptProperties 로드됨. API Key 설정 여부: {}, API URL: {}",
                (gptProperties.getApiKey() != null && !gptProperties.getApiKey().isEmpty()) ? "설정됨" : "설정 안됨 또는 비어있음",
                gptProperties.getApiUrl());

        // API URL이 정말 null인지 다시 한번 확인
        if (gptProperties.getApiUrl() == null) {
            log.error("GPT API URL이 null입니다. application.properties 설정을 확인해주세요.");
            return "설정 오류: GPT API URL이 설정되지 않았습니다.";
        }
        // API Key가 정말 null이거나 비어있는지 확인
        if (gptProperties.getApiKey() == null || gptProperties.getApiKey().isEmpty()) {
            log.error("GPT API Key가 null이거나 비어있습니다. application.properties 설정을 확인해주세요.");
            return "설정 오류: GPT API Key가 설정되지 않았습니다.";
        }
        // Assert.notNull(gptProperties.getApiUrl(), "GPT API URL must not be null"); // Assert를 사용하는 방법도 있음

        log.info("GPT API 호출 시작. API URL: {}", gptProperties.getApiUrl()); // 이제 이 로그가 보여야 합니다.

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptProperties.getApiKey()); // GptProperties에서 API Key 사용

        GptChatMessageDto systemMessage = new GptChatMessageDto("system", "당신은 채용 시장 동향을 요약해주는 친절한 AI 어시스턴트입니다. 신입 개발자 구직자에게 희망적이면서도 현실적인 조언을 담아 코멘트를 작성합니다. 항상 한국어로 답변해주세요.");
        GptChatMessageDto userMessage = new GptChatMessageDto("user", prompt);

        // Java 9 이상에서는 List.of() 사용 가능
        // List<GptChatMessageDto> messages = List.of(systemMessage, userMessage);
        // Java 8 이하에서는 Arrays.asList() 사용
        List<GptChatMessageDto> messages = Arrays.asList(systemMessage, userMessage);


        GptChatRequestDto requestDto = GptChatRequestDto.builder()
                .model("gpt-3.5-turbo") // 사용할 모델 (필요시 gpt-4 등으로 변경)
                .messages(messages)
                .max_tokens(250)        // 답변 최대 길이 (코멘트 길이에 맞춰 조정)
                .temperature(0.7)       // 답변의 창의성/일관성 조절 (0.0 ~ 2.0)
                .build();

        HttpEntity<GptChatRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<GptChatResponseDto> response = restTemplate.postForEntity(
                    gptProperties.getApiUrl(), // GptProperties에서 API URL 사용
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
