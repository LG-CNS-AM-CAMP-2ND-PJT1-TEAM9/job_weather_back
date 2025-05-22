package com.example.job_weather_back.controller;

import com.example.job_weather_back.dto.NewsDto;
import com.example.job_weather_back.entity.LikedNews;
import com.example.job_weather_back.entity.News;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.LikedNewsRepository;
import com.example.job_weather_back.repository.NewsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Locale;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.parameters.RequestBody; // Spring의 RequestBody와 이름이 같으므로, 사용할 때 정규화된 이름 사용
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "News API", description = "뉴스 조회, 검색 및 찜하기 관련 API")
@RestController
@RequestMapping("/news")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", allowCredentials = "true")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private LikedNewsRepository likedNewsRepository;

    // 네이버 뉴스 API 키
    @Value("${naver.news.client-id}")
    private String clientId;

    @Value("${naver.news.client-secret}")
    private String clientSecret;

    // 채용 키워드 리스트
    private static final List<String> KEYWORDS = Arrays.asList(
        "취업", "채용", "구인", "일자리",
        "신입", "경력", "공채", "채용시장",
        "취업난", "취업률", "채용정보", "채용공고"
    );

    // 프론트의 뉴스 검색 응답
    @Operation(summary = "뉴스 목록 조회 또는 검색", description = "검색어(search) 유무에 따라 전체 최신 뉴스 목록 또는 검색 결과를 반환합니다. 검색어가 없으면 모든 뉴스를 최신순으로 반환합니다.")
    @ApiResponses(value = {
            // 응답 스키마를 Map 또는 Object로 변경
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                                 schema = @Schema(type = "object", example = "{\"items\": [{\"newsSn\":1, ...}], \"total\": 1}"))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getNews(@RequestParam(required = false) String search) {
        try {
            List<News> newsList;
            if (search != null && !search.trim().isEmpty()) {
                // 검색어가 있는 경우 제목이나 설명에 검색어가 포함된 뉴스 검색
                newsList = newsRepository.findByNewsTitleContainingOrNewsDescriptionContaining(
                    search, search
                );
            } else {
                // 검색어가 없는 경우 모든 뉴스를 날짜순으로 반환
                newsList = newsRepository.findAllByOrderByNewsDateTimeDesc();
            }

            // News 엔티티를 NewsDto로 변환
            List<NewsDto> newsDtoList = newsList.stream()
                .map(news -> new NewsDto(
                    news.getNewsSn(),
                    news.getNewsTitle(),
                    news.getNewsDescription(),
                    news.getNewsLink(),
                    news.getNewsDateTime()
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "items", newsDtoList,
                "total", newsDtoList.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("뉴스를 불러오는데 실패했습니다: " + e.getMessage());
        }
    }

    //뉴스 가져오기 스케쥴링(30분)
    @Scheduled(fixedRate = 30 * 60 * 1000) 
    public void scheduledFetchNews() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("뉴스 가져오기 시작: " + LocalDateTime.now().format(formatter));
            ResponseEntity<?> response = fetchNews();
            System.out.println(response.getBody());
            System.out.println("뉴스 가져오기 완료: " + LocalDateTime.now().format(formatter));
        } catch (Exception e) {
            System.err.println("뉴스 가져오기 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 뉴스 가져와서 데이터베이스에 저장
    private ResponseEntity<?> fetchNews() throws Exception {
        // 키워드를 OR 연산자(|)로 연결
        String searchQuery = String.join(" | ", KEYWORDS);
        String encodedKeyword = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());

        // DB에서 마지막으로 저장된 뉴스의 날짜 조회
        LocalDateTime lastSavedDate = newsRepository.findTopByOrderByNewsDateTimeDesc()
            .map(news -> news.getNewsDateTime())
            .orElse(LocalDateTime.MIN);

        // 현재 시간을 기준으로 설정
        LocalDateTime currentTime = LocalDateTime.now();
        
        int totalSavedCount = 0;
        int totalFoundCount = 0;
        List<News> newsToSave = new ArrayList<>();

        // 최대 1000개까지 가져오기 위해 10번 반복 (100개씩)
        for (int start = 1; start <= 1000; start += 100) {
            // API 요청
            String urlStr = String.format(
                "https://openapi.naver.com/v1/search/news.json?query=%s&display=100&sort=date&start=%d",
                encodedKeyword, start
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Naver-Client-Id", clientId);
            conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            // 응답 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode items = root.get("items");
            totalFoundCount = root.get("total").asInt();

            if (items == null || !items.isArray() || items.size() == 0) {
                break;
            }

            // 뉴스 수집
            for (JsonNode item : items) {
                String pubDateStr = item.get("pubDate").asText();
                
                LocalDateTime newsDate = LocalDateTime.parse(
                    pubDateStr,
                    DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
                );

                // 마지막 저장된 뉴스보다 최신이고 현재 시간 이전인 경우만 저장
                if (newsDate.isAfter(lastSavedDate) && !newsDate.isAfter(currentTime)) {
                    String title = item.get("title").asText().replaceAll("<[^>]*>", "");
                    String description = item.get("description").asText().replaceAll("<[^>]*>", "");
                    
                    // 제목이나 설명에 키워드가 포함되어 있는지 확인
                    boolean containsKeyword = KEYWORDS.stream()
                        .anyMatch(keyword -> 
                            title.contains(keyword) || description.contains(keyword)
                        );

                    if (containsKeyword) {
                        News news = new News();
                        news.setNewsTitle(title);
                        news.setNewsDescription(description);
                        news.setNewsLink(item.get("link").asText());
                        news.setNewsDateTime(newsDate);
                        newsToSave.add(news);
                    }
                }
            }
        }

        // 수집된 뉴스를 시간순으로 정렬
        newsToSave.sort(Comparator.comparing(News::getNewsDateTime));

        // 정렬된 뉴스를 저장
        for (News news : newsToSave) {
            newsRepository.save(news);
            totalSavedCount++;
        }

        return ResponseEntity.ok(String.format(
            "검색된 뉴스: %d개, 저장된 뉴스: %d개 (마지막 저장 이후의 뉴스만 저장)",
            totalFoundCount, totalSavedCount
        ));
    }

    // 찜한 뉴스 목록 조회
    @Operation(summary = "찜한 뉴스 ID 목록 조회", description = "현재 로그인된 사용자가 찜한 뉴스의 ID 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = Integer.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content)
    })
    @GetMapping("/liked")
    public ResponseEntity<?> getLikedNews(@SessionAttribute("user_info") User user) {
        List<LikedNews> likedNewsList = likedNewsRepository.findByUserUserSn(user.getUserSn());
        List<Integer> likedNewsIds = likedNewsList.stream()
                .map(likedNews -> likedNews.getNews().getNewsSn())
                .collect(Collectors.toList());

        return ResponseEntity.ok(likedNewsIds); 
    }

    // 뉴스 찜하기/찜하기 취소
    @Operation(summary = "뉴스 찜하기/찜 취소 (토글)", description = "특정 뉴스를 찜하거나 찜 목록에서 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "찜하기/찜 취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 뉴스 ID 누락 또는 존재하지 않는 뉴스)", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content)
    })
    @PostMapping("/like")
    public ResponseEntity<?> toggleLike(
            @RequestBody Map<String, Integer> body,
            @SessionAttribute("user_info") User user) {
        
        // 요청에서 뉴스 ID 체크
        Integer newsSn = body.get("newsSn");
        if (newsSn == null) {
            return ResponseEntity.status(400).body(Map.of("message", "뉴스 ID가 필요합니다."));
        }

        // 뉴스 조회
        News news = newsRepository.findById(newsSn).orElse(null);

        if (news == null) {
            return ResponseEntity.status(400).body(Map.of("message", "존재하지 않는 뉴스입니다."));
        }

        // 찜하기 토글
        Optional<LikedNews> existingLike = likedNewsRepository.findByUserUserSnAndNewsNewsSn(user.getUserSn(), newsSn);
        
        if (existingLike.isPresent()) {
            likedNewsRepository.delete(existingLike.get());
            return ResponseEntity.ok().build();
        } else {
            LikedNews likedNews = new LikedNews();
            likedNews.setUser(user);
            likedNews.setNews(news);
            likedNewsRepository.save(likedNews);
            return ResponseEntity.ok().build();
        }
    }
} 