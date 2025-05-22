package com.example.job_weather_back.controller.mypage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_weather_back.entity.LikedEmployment;
import com.example.job_weather_back.entity.LikedNews;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.LikedEmploymentRepository;
import com.example.job_weather_back.repository.LikedNewsRepository;
import com.example.job_weather_back.service.LikedEmpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Tag(name = "Liked Content API (Mypage)", description = "마이페이지 - 사용자가 찜한 뉴스 및 채용공고 관리 API")
@CrossOrigin
@RestController
public class LikedController {
    @Autowired LikedNewsRepository likedNewsRepository;
    @Autowired LikedEmploymentRepository likedEmploymentRepository;
    @Autowired LikedEmpService likedEmpService;

    // 스크랩한 뉴스 제목 + url 반환
    @Operation(summary = "찜한 뉴스 목록 조회", description = "현재 로그인된 사용자가 찜한 뉴스 목록을 반환합니다. (날짜 시간순 정렬)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", example = "[{\"id\": 101, \"title\": \"뉴스 제목\", \"url\": \"http://...\", \"description\": \"...\", \"date\": \"2025-05-22T10:30:00\"}]"))),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content)
    })
    @GetMapping("/mypage/liked/news")
    public ResponseEntity<?> getLikedNews(HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        
        int userSn = user.getUserSn();
        List<LikedNews> likedNewsList = likedNewsRepository.findByUserUserSn(userSn);

        List<Map<String,Object>> result = likedNewsList.stream()
        .sorted(Comparator.comparing(likedNews -> likedNews.getNews().getNewsDateTime()))
            .map(likedNews-> {
                Map<String,Object> map = new HashMap<>();
                map.put("id",likedNews.getNews().getNewsSn());
                map.put("title", likedNews.getNews().getNewsTitle());
                map.put("url", likedNews.getNews().getNewsLink());
                map.put("description", likedNews.getNews().getNewsDescription());
                map.put("date", likedNews.getNews().getNewsDateTime());

                return map;
            }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 스크랩한 채용공고 반환
    @Operation(summary = "찜한 채용공고 목록 조회", description = "현재 로그인된 사용자가 찜한 채용공고 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", example = "[{\"empNum\": 201, \"companyName\": \"회사명\", ...}]"))), // 실제 반환 DTO로 변경
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content)
    })
    @GetMapping("/mypage/liked/emp")
    public ResponseEntity<?> getLikedEmp(HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }

        int userSn = user.getUserSn();
        List<LikedEmployment> likedEmpList = likedEmploymentRepository.findByUserUserSn(userSn);

        List<Map<String,Object>> empInfoList = likedEmpList.stream()
            .map(emp -> {
                System.out.println("empNum"+emp.getEmpNum());
                System.out.println("liked info:" + likedEmpService.getLikedEmpInfoByNum(emp.getEmpNum()));
                return likedEmpService.getLikedEmpInfoByNum(emp.getEmpNum());
            }).collect(Collectors.toList());

        return ResponseEntity.ok(empInfoList);
    }
    
    
    // 스크랩 취소
    // 추가: DeleteMapping으로 바꾸는 게 좋다고 합니다. (RESTful 원칙...)
    @Operation(summary = "찜한 항목 삭제 (뉴스 또는 채용공고)", description = "현재 로그인된 사용자의 찜 목록에서 특정 뉴스 또는 채용공고를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"success\": true, \"message\": \"스크랩 취소 완료\"}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (item 종류 또는 itemId 오류)", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content),
            @ApiResponse(responseCode = "404", description = "삭제할 찜한 항목을 찾을 수 없음", content = @Content)
    })
    @Transactional
    @PostMapping("/mypage/unliked/{item}/{itemId}")
    public ResponseEntity<?> deletLiked(
            @PathVariable String item,
            @PathVariable int itemId,
            HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }

        int usersn = user.getUserSn();

        if(item.equals("news")) {
            likedNewsRepository.deleteByUserUserSnAndLikedNewsId(usersn, itemId);
        } else if(item.equals("emp")) {
            likedEmploymentRepository.deleteByUserUserSnAndLikedEmpId(usersn, itemId);
        } else {
            return ResponseEntity.badRequest().body("오류가 발생했습니다");
        }

        return ResponseEntity.ok(Map.of("success",true,"message","스크랩 취소 완료"));
    }
    
}
