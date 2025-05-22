package com.example.job_weather_back.controller.mypage;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Mypage API", description = "마이페이지 관련 기본 API (예: 로그인 상태 확인)")
@CrossOrigin
@RestController
public class MypageController {

    @Operation(summary = "로그인 상태 확인", description = "현재 사용자의 로그인 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 확인됨",
                         content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"로그인 확인\"}"))),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)",
                         content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"로그인이 필요합니다.\"}")))
    })
    @GetMapping("/mypage/check")
    public ResponseEntity<?> getMethodName(HttpSession session) {
        if(session.getAttribute("user_info") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        return ResponseEntity.ok("로그인 확인");
    }
    
}
