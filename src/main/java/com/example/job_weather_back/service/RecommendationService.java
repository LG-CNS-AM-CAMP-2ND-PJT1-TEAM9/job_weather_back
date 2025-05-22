package com.example.job_weather_back.service;

import com.example.job_weather_back.dto.NewsDto;
import com.example.job_weather_back.dto.MainPageRecommendationsDto;
import com.example.job_weather_back.entity.CompanyType;
import com.example.job_weather_back.entity.CustomLocation;
import com.example.job_weather_back.entity.CustomPosition;
import com.example.job_weather_back.entity.News;
import com.example.job_weather_back.entity.UserRecommendation;
import com.example.job_weather_back.repository.NewsRepository;
import com.example.job_weather_back.repository.UserRecommendationRepository;
import com.example.job_weather_back.repository.CompanyTypeRepository;
import com.example.job_weather_back.repository.CustomPositionRepository;
import com.example.job_weather_back.repository.CustomLocationRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
// import org.springframework.scheduling.annotation.Scheduled; // 스케줄러 어노테이션 제거
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final Integer GENERAL_USER_SN = 0;
    private static final int MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE = 8; // 이전 값 유지

    private final UserRecommendationRepository userRecommendationRepository;
    private final NewsRepository newsRepository;

    private final CompanyTypeRepository companyTypeRepository;
    private final CustomPositionRepository customPositionRepository;
    private final CustomLocationRepository customLocationRepository;

    @Value("${naver.news.client-id}")
    private String naverClientId;
    @Value("${naver.news.client-secret}")
    private String naverClientSecret;

    private static final List<String> BASE_NEWS_KEYWORDS = Arrays.asList(
        "취업", "채용", "IT기술", "개발자", "프로그래밍", "코딩", "스타트업", "인공지능", "AI", "클라우드",
        "빅데이터", "SW개발", "소프트웨어 엔지니어", "신입 채용", "개발자 채용", "기술 트렌드", "커리어"
    );

    @Autowired
    public RecommendationService(UserRecommendationRepository userRecommendationRepository,
                                 NewsRepository newsRepository,
                                 CompanyTypeRepository companyTypeRepository,
                                 CustomPositionRepository customPositionRepository,
                                 CustomLocationRepository customLocationRepository) {
        this.userRecommendationRepository = userRecommendationRepository;
        this.newsRepository = newsRepository;
        this.companyTypeRepository = companyTypeRepository;
        this.customPositionRepository = customPositionRepository;
        this.customLocationRepository = customLocationRepository;
    }

    @Transactional
    // @Scheduled(cron = "0 0/30 * * * ?") // 스케줄러 제거
    public void fetchAndSaveNewsFromNaverApi() { // 이제 public으로 유지 (Controller에서 호출 가능하도록)
        // ... (메소드 내용은 이전과 동일) ...
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.info("추천용 뉴스 수집 시작 (요청 시 실행): {}", LocalDateTime.now().format(formatter));
        try {
            List<String> currentKeywords = getAugmentedKeywordsForNewsSearch();
            if (currentKeywords.isEmpty()) {
                log.warn("뉴스 검색을 위한 키워드가 없어 뉴스 수집을 건너<0xEB><0x84>니다.");
                return;
            }

            String searchQuery = currentKeywords.stream().distinct().limit(10).collect(Collectors.joining(" | "));
            String encodedKeyword = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());

            LocalDateTime lastSavedDate = newsRepository.findTopByOrderByNewsDateTimeDesc()
                .map(News::getNewsDateTime)
                .orElse(LocalDateTime.now().minusDays(3));
            
            LocalDateTime fetchUntilDate = LocalDateTime.now();
            int totalSavedCount = 0;
            final int MAX_API_PAGES_TO_FETCH = 1; // 매번 호출되므로 API 호출 페이지 수 최소화 (예: 100개만)
            int pagesFetched = 0;

            log.info("Naver 뉴스 API 검색 시작. 검색어 일부: '{}', 기준일시: {}", searchQuery.substring(0, Math.min(searchQuery.length(), 50)), lastSavedDate);

            for (int start = 1; start <= 100 && pagesFetched < MAX_API_PAGES_TO_FETCH; start += 100) { // 최대 100개만 가져오도록 수정
                pagesFetched++;
                String urlStr = String.format(
                    "https://openapi.naver.com/v1/search/news.json?query=%s&display=100&sort=date&start=%d",
                    encodedKeyword, start);
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // ... (이하 API 호출 로직은 이전과 동일) ...
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Naver-Client-Id", naverClientId);
                conn.setRequestProperty("X-Naver-Client-Secret", naverClientSecret);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    log.error("Naver 뉴스 API 요청 실패. URL: {}, 응답 코드: {}", urlStr, responseCode);
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        log.error("Naver 뉴스 API 에러 응답: {}", errorReader.lines().collect(Collectors.joining("\n")));
                    } catch (Exception e) { /* ignore */ }
                    break; 
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseBody = reader.lines().collect(Collectors.joining("\n"));
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(responseBody);
                    JsonNode items = root.path("items");

                    if (!items.isArray() || items.isEmpty()) {
                        log.info("Naver 뉴스 API 응답에 'items'가 없거나 비어있음 (start: {}).", start);
                        break; 
                    }

                    List<News> newsBatchToSave = new ArrayList<>();
                    for (JsonNode item : items) {
                        String pubDateStr = item.path("pubDate").asText();
                        LocalDateTime newsDate;
                        try {
                            newsDate = LocalDateTime.parse(pubDateStr, DateTimeFormatter.ofPattern("EEE, dd MMM uuuu HH:mm:ss Z", Locale.ENGLISH));
                        } catch (DateTimeParseException e) {
                            log.warn("뉴스 발행일 파싱 실패: '{}'. 오류: {}", pubDateStr, e.getMessage());
                            continue;
                        }

                        if (newsDate.isAfter(lastSavedDate) && !newsDate.isAfter(fetchUntilDate.plusDays(1)) && !newsDate.isAfter(fetchUntilDate)) {
                            String title = item.path("title").asText().replaceAll("<[^>]*>", "").trim();
                            String description = item.path("description").asText().replaceAll("<[^>]*>", "").trim();
                            String link = item.path("link").asText();
                            String originallink = item.path("originallink").asText();

                            String finalLink = (originallink != null && !originallink.isEmpty()) ? originallink : link;

                            if (newsRepository.existsByNewsLink(finalLink)) {
                                continue;
                            }

                            News news = new News();
                            news.setNewsTitle(title);
                            news.setNewsDescription(description);
                            news.setNewsLink(finalLink);
                            news.setNewsDateTime(newsDate);
                            newsBatchToSave.add(news);
                        }
                    }

                    if (!newsBatchToSave.isEmpty()) {
                        newsBatchToSave.sort(Comparator.comparing(News::getNewsDateTime));
                        newsRepository.saveAll(newsBatchToSave);
                        totalSavedCount += newsBatchToSave.size();
                        log.info("{}개의 새로운 뉴스 저장 완료 (start: {}).", newsBatchToSave.size(), start);
                    } else {
                        log.info("저장할 신규 뉴스가 없습니다 (start: {}).", start);
                    }
                } 
            }
            log.info("뉴스 수집 완료. 총 {}개의 뉴스 신규 저장.", totalSavedCount);

        } catch (Exception e) {
            log.error("뉴스 수집 중 오류 발생", e); // 로그 메시지 명확화
        }
    }

    private List<String> getAugmentedKeywordsForNewsSearch() {
        // ... (메소드 내용은 이전과 동일) ...
        Set<String> augmentedKeywords = new HashSet<>(BASE_NEWS_KEYWORDS);
        try {
            companyTypeRepository.findAll().stream()
                .map(CompanyType::getTypeName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .forEach(augmentedKeywords::add);

            customPositionRepository.findAll().stream()
                .map(CustomPosition::getPositionName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .forEach(augmentedKeywords::add);

            customLocationRepository.findAll().stream()
                .map(CustomLocation::getLocationName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .forEach(augmentedKeywords::add);

            log.info("DB에서 추가 키워드 조회 완료. 현재 키워드 개수: {}", augmentedKeywords.size());
        } catch (Exception e) {
            log.error("DB에서 추가 키워드 조회 중 오류 발생", e);
        }
        if (augmentedKeywords.isEmpty()) {
            log.warn("확장된 검색 키워드가 하나도 없습니다. 기본 키워드만 사용합니다.");
            return BASE_NEWS_KEYWORDS.isEmpty() ? Collections.singletonList("채용") : new ArrayList<>(BASE_NEWS_KEYWORDS);
        }
        return new ArrayList<>(augmentedKeywords);
    }

    @Transactional
    // @Scheduled(cron = "0 5/30 * * * ?") // 스케줄러 제거
    public void populateGeneralRecommendations() { // 이제 public으로 유지
        // ... (메소드 내용은 이전과 동일) ...
        log.info("일반 사용자({})를 위한 추천 콘텐츠 생성 시작 (요청 시 실행)...", GENERAL_USER_SN);
        userRecommendationRepository.deleteByUserSnIsNullAndContentType(UserRecommendation.ContentType.NEWS);
        log.info("기존 일반 사용자 뉴스 추천 삭제 완료.");

        Pageable newsPageable = PageRequest.of(0, MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE, Sort.by(Sort.Direction.DESC, "newsDateTime"));
        List<News> latestNewsForRecommendation = newsRepository.findAll(newsPageable).getContent();

        for (News news : latestNewsForRecommendation) {
            if (!userRecommendationRepository.existsByUserSnIsNullAndContentTypeAndTargetSn(
                    UserRecommendation.ContentType.NEWS, news.getNewsSn())) {
                UserRecommendation recommendation = UserRecommendation.builder()
                        .userSn(null)
                        .contentType(UserRecommendation.ContentType.NEWS)
                        .targetSn(news.getNewsSn())
                        .build();
                userRecommendationRepository.save(recommendation);
            }
        }
        log.info("{}개의 뉴스 추천 저장 완료 (일반 사용자용).", latestNewsForRecommendation.size());
        log.warn("채용공고 추천 로직은 사람인 API 연동 후 구현 예정입니다 (일반 사용자용).");
        log.info("일반 사용자 추천 콘텐츠 생성 완료.");
    }

    @Transactional(readOnly = true)
    public MainPageRecommendationsDto getRecommendationsForMainPage() {
        // ... (메소드 내용은 이전과 동일) ...
        Integer userIdToFetch = null; 
        boolean isLoggedIn = false; 

        List<UserRecommendation> newsRecs;
        if (isLoggedIn && userIdToFetch != null) {
            newsRecs = userRecommendationRepository.findByUserSnAndContentTypeOrderByIdDesc(
                    userIdToFetch, UserRecommendation.ContentType.NEWS);
        } else {
            newsRecs = userRecommendationRepository.findByUserSnIsNullAndContentTypeOrderByIdDesc(
                    UserRecommendation.ContentType.NEWS);
        }
        
        List<NewsDto> recommendedNewsDtos = newsRecs.stream()
                .map(rec -> newsRepository.findById(rec.getTargetSn()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(news -> new NewsDto(
                        news.getNewsSn(),
                        news.getNewsTitle(),
                        news.getNewsDescription(),
                        news.getNewsLink(),
                        news.getNewsDateTime()))
                .limit(MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE)
                .collect(Collectors.toList());
        
        String userTypeForLog = isLoggedIn ? "로그인 사용자 (ID: " + userIdToFetch + ")" : "일반 (userSn is NULL)";
        log.info("메인 페이지 추천 조회 ({}): 뉴스 {}건", userTypeForLog, recommendedNewsDtos.size());

        return MainPageRecommendationsDto.builder()
                .news(recommendedNewsDtos)
                .build();
    }
}
