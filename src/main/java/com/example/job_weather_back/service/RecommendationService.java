package com.example.job_weather_back.service;

import com.example.job_weather_back.dto.NewsDto;
import com.example.job_weather_back.dto.MainPageRecommendationsDto;
import com.example.job_weather_back.entity.*; // User, CompanyType, Customization 등 엔티티 import
import com.example.job_weather_back.repository.*; // 모든 필요한 Repository import

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final int MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE = 12;

    private final UserRecommendationRepository userRecommendationRepository;
    private final NewsRepository newsRepository;
    private final CustomizationRepository customizationRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final CustomPositionRepository customPositionRepository;
    private final CustomLocationRepository customLocationRepository;
    // private final UserRepository userRepository; // UserRepository가 있다면 주입

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
                                 CustomizationRepository customizationRepository, // 주입 추가
                                 CompanyTypeRepository companyTypeRepository,
                                 CustomPositionRepository customPositionRepository,
                                 CustomLocationRepository customLocationRepository
                                 /* UserRepository userRepository */) { // UserRepository 주입
        this.userRecommendationRepository = userRecommendationRepository;
        this.newsRepository = newsRepository;
        this.customizationRepository = customizationRepository;
        this.companyTypeRepository = companyTypeRepository;
        this.customPositionRepository = customPositionRepository;
        this.customLocationRepository = customLocationRepository;
        // this.userRepository = userRepository;
    }

    // ... fetchAndSaveNewsFromNaverApi() 와 getAugmentedKeywordsForNewsSearch()는 이전과 동일 ...
    @Transactional
    @Scheduled(cron = "0 0/30 * * * ?")
    public void fetchAndSaveNewsFromNaverApi() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.info("추천용 뉴스 수집 스케줄링 시작: {}", LocalDateTime.now().format(formatter));
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
            final int MAX_API_PAGES_TO_FETCH = 3; 
            int pagesFetched = 0;

            log.info("Naver 뉴스 API 검색 시작. 검색어 일부: '{}', 기준일시: {}", searchQuery.substring(0, Math.min(searchQuery.length(), 50)), lastSavedDate);

            for (int start = 1; start <= 300 && pagesFetched < MAX_API_PAGES_TO_FETCH; start += 100) {
                pagesFetched++;
                String urlStr = String.format(
                    "https://openapi.naver.com/v1/search/news.json?query=%s&display=100&sort=date&start=%d",
                    encodedKeyword, start);
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
            log.error("뉴스 수집 스케줄링 중 오류 발생", e);
        }
    }

    private List<String> getAugmentedKeywordsForNewsSearch() {
        Set<String> augmentedKeywords = new HashSet<>(BASE_NEWS_KEYWORDS);
        try {
            companyTypeRepository.findAll().stream().map(CompanyType::getTypeName).filter(Objects::nonNull).forEach(augmentedKeywords::add);
            customPositionRepository.findAll().stream().map(CustomPosition::getPositionName).filter(Objects::nonNull).forEach(augmentedKeywords::add);
            customLocationRepository.findAll().stream().map(CustomLocation::getLocationName).filter(Objects::nonNull).forEach(augmentedKeywords::add);
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
    
    /**
     * 일반 사용자를 위한 추천 콘텐츠를 주기적으로 생성합니다.
     */
    @Transactional
    @Scheduled(cron = "0 5/30 * * * ?")
    public void populateGeneralRecommendations() {
        log.info("일반 사용자 (user_sn is NULL)를 위한 추천 콘텐츠 생성 시작...");
        userRecommendationRepository.deleteByUserSnIsNullAndContentType(UserRecommendation.ContentType.NEWS);
        log.info("기존 일반 사용자 뉴스 추천 삭제 완료.");

        Pageable newsPageable = PageRequest.of(0, MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE, Sort.by(Sort.Direction.DESC, "newsDateTime"));
        List<News> latestNewsForRecommendation = newsRepository.findAll(newsPageable).getContent();

        saveRecommendations(null, latestNewsForRecommendation, UserRecommendation.ContentType.NEWS); // userSn에 null 전달
        log.info("{}개의 뉴스 추천 저장 완료 (일반 사용자용).", latestNewsForRecommendation.size());
        log.info("일반 사용자 추천 콘텐츠 생성 완료.");
    }

    /**
     * 특정 로그인 사용자를 위한 맞춤형 추천 콘텐츠를 생성합니다.
     */
    @Transactional
    public void populateUserSpecificRecommendations(Integer userSn) { // 파라미터 타입을 Integer로 변경
        if (userSn == null) {
            log.warn("populateUserSpecificRecommendations 호출 시 userSn이 null입니다. 일반 추천을 사용해야 합니다.");
            return;
        }
        log.info("{} 사용자를 위한 맞춤 추천 콘텐츠 생성 시작...", userSn);

        userRecommendationRepository.deleteByUserSnAndContentType(userSn, UserRecommendation.ContentType.NEWS);
        log.info("{} 사용자의 기존 뉴스 추천 삭제 완료.", userSn);

        Set<String> userInterestKeywords = new HashSet<>();
        // CustomizationRepository의 findByUserUserSn 메소드 사용 (User 엔티티의 PK가 int라고 가정)
        Optional<Customization> userCustomizationOpt = customizationRepository.findByUserUserSn(userSn);

        if (userCustomizationOpt.isPresent()) {
            Customization custom = userCustomizationOpt.get();
            if (custom.getCompanyType() != null && custom.getCompanyType().getTypeName() != null) {
                userInterestKeywords.add(custom.getCompanyType().getTypeName());
            }
            if (custom.getCustomPosition() != null && custom.getCustomPosition().getPositionName() != null) {
                userInterestKeywords.add(custom.getCustomPosition().getPositionName());
            }
            if (custom.getCustomLocation() != null && custom.getCustomLocation().getLocationName() != null) {
                userInterestKeywords.add(custom.getCustomLocation().getLocationName());
            }
        }
        // (선택적) 찜한 뉴스의 키워드도 추가

        List<News> newsToRecommend;
        if (userInterestKeywords.isEmpty()) {
            log.info("{} 사용자가 설정한 관심사가 없어 기본 최신 뉴스로 추천합니다.", userSn);
            Pageable newsPageable = PageRequest.of(0, MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE, Sort.by(Sort.Direction.DESC, "newsDateTime"));
            newsToRecommend = newsRepository.findAll(newsPageable).getContent();
        } else {
            log.info("{} 사용자의 관심 키워드: {}", userSn, userInterestKeywords);
            // 임시: 모든 뉴스에서 필터링 (NewsRepository에 키워드 검색 메소드 추가 권장)
            List<News> allNews = newsRepository.findAll(Sort.by(Sort.Direction.DESC, "newsDateTime"));
            newsToRecommend = allNews.stream()
                .filter(news -> userInterestKeywords.stream().anyMatch(keyword ->
                    (news.getNewsTitle() != null && news.getNewsTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                    (news.getNewsDescription() != null && news.getNewsDescription().toLowerCase().contains(keyword.toLowerCase()))
                ))
                .limit(MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE)
                .collect(Collectors.toList());
        }
        
        saveRecommendations(userSn, newsToRecommend, UserRecommendation.ContentType.NEWS);
        log.info("{} 사용자를 위한 맞춤 뉴스 추천 {}건 저장 완료.", userSn, newsToRecommend.size());
        log.info("{} 사용자 맞춤 추천 콘텐츠 생성 완료.", userSn);
    }

    private void saveRecommendations(Integer userSn, List<News> newsList, UserRecommendation.ContentType contentType) {
        for (News news : newsList) {
            boolean exists;
            if (userSn == null) {
                exists = userRecommendationRepository.existsByUserSnIsNullAndContentTypeAndTargetSn(contentType, news.getNewsSn());
            } else {
                exists = userRecommendationRepository.existsByUserSnAndContentTypeAndTargetSn(userSn, contentType, news.getNewsSn());
            }

            if (!exists) {
                UserRecommendation recommendation = UserRecommendation.builder()
                        .userSn(userSn) // null 또는 실제 userSn
                        .contentType(contentType)
                        .targetSn(news.getNewsSn())
                        .build();
                userRecommendationRepository.save(recommendation);
            }
        }
    }

    @Transactional(readOnly = true)
    public MainPageRecommendationsDto getRecommendationsForMainPage(HttpSession session) {
        List<UserRecommendation> newsRecs;
        boolean isLoggedIn = false;
        Integer userIdToFetch = null; // User 엔티티의 PK 타입이 int이므로 Integer 사용

        User loggedInUser = (User) session.getAttribute("user_info");

        if (loggedInUser != null) {
            isLoggedIn = true;
            userIdToFetch = loggedInUser.getUserSn(); // User 엔티티의 PK (int)
            log.info("로그인한 사용자 ({}) 추천을 가져옵니다.", userIdToFetch);
            newsRecs = userRecommendationRepository.findByUserSnAndContentTypeOrderByIdDesc(
                    userIdToFetch, UserRecommendation.ContentType.NEWS);
            
            // 로그인한 사용자의 추천이 없거나 적으면, 일반 추천으로 보강하거나 즉시 생성 시도 (선택적)
            if (newsRecs.isEmpty()) {
                log.info("로그인한 사용자 ({})의 맞춤 뉴스가 없습니다. 일반 추천으로 대체하거나, 맞춤 추천 생성을 시도합니다.", userIdToFetch);
                // populateUserSpecificRecommendations(userIdToFetch); // 즉시 생성은 API 응답 지연 유발 가능
                // newsRecs = userRecommendationRepository.findByUserSnAndContentTypeOrderByIdDesc(userIdToFetch, UserRecommendation.ContentType.NEWS);
                // 만약 그래도 없다면 일반 추천으로 대체
                if (newsRecs.isEmpty()) {
                    newsRecs = userRecommendationRepository.findByUserSnIsNullAndContentTypeOrderByIdDesc(UserRecommendation.ContentType.NEWS);
                }
            }

        } else {
            log.info("비로그인 사용자 (일반) 추천을 가져옵니다.");
            newsRecs = userRecommendationRepository.findByUserSnIsNullAndContentTypeOrderByIdDesc(
                    UserRecommendation.ContentType.NEWS);
        }

        List<NewsDto> recommendedNewsDtos = newsRecs.stream()
                .map(rec -> newsRepository.findById(rec.getTargetSn())) // NewsRepository의 findById 사용
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(news -> new NewsDto(
                        news.getNewsSn(),
                        news.getNewsTitle(),
                        news.getNewsDescription(),
                        news.getNewsLink(),
                        news.getNewsDateTime()))
                .limit(MAX_NEWS_RECOMMENDATIONS_FOR_MAIN_PAGE) // 최종적으로 보여줄 개수 제한
                .collect(Collectors.toList());
        
        String userTypeForLog = isLoggedIn ? "로그인 사용자 (ID: " + userIdToFetch + ")" : "일반 (userSn is NULL)";
        log.info("메인 페이지 추천 조회 ({}): 뉴스 {}건", userTypeForLog, recommendedNewsDtos.size());

        return MainPageRecommendationsDto.builder()
                .news(recommendedNewsDtos)
                .build();
    }
}
