package com.example.job_weather_back.controller.mypage;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;


@CrossOrigin
@RestController
public class MypageController {
    @GetMapping("/mypage/check")
    public ResponseEntity<?> getMethodName(HttpSession session) {
        if(session.getAttribute("user_info") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        return ResponseEntity.ok("로그인 확인");
    }
    
}
