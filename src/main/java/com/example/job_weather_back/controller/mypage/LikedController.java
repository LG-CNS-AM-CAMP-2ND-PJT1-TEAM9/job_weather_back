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

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@CrossOrigin
@RestController
public class LikedController {
    @Autowired LikedNewsRepository likedNewsRepository;
    @Autowired LikedEmploymentRepository likedEmploymentRepository;
    @Autowired LikedEmpService likedEmpService;

    // 스크랩한 뉴스 제목 + url 반환
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
