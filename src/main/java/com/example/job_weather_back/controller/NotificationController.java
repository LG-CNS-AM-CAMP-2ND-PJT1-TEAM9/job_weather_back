package com.example.job_weather_back.controller;

import com.example.job_weather_back.dto.NotificationDto;
import com.example.job_weather_back.entity.Notification;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.NotificationRepository;
import com.example.job_weather_back.repository.UserRepository;
import com.example.job_weather_back.service.UserMatchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Tag(name = "Notification API", description = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserMatchService userMatchService;

    // 로그인 여부 확인
    @GetMapping("/isLogin")
    public ResponseEntity<?> isLogin(HttpSession session) {
        User user_info = (User) session.getAttribute("user_info");
        if(user_info == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(("로그인 필요"));
        }
        return ResponseEntity.ok(user_info);

    }
    

    // 모든 알림 조회
    @Operation(summary = "모든 알림 조회 (관리자용)", description = "시스템에 저장된 모든 사용자의 알림 목록을 반환합니다. (주로 관리자 기능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", implementation = Notification.class)))
    })
    @GetMapping
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    // 특정 사용자 알림만 조회
    @Operation(summary = "특정 사용자 알림 조회", description = "경로 변수로 전달된 특정 사용자의 모든 알림 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", implementation = Notification.class))),
            @ApiResponse(responseCode = "404", description = "해당 사용자의 알림 없음 (빈 리스트 반환 가능)", content = @Content)
    })
    @GetMapping("/user/{userSn}")
    public List<Notification> getByUser(@PathVariable Integer userSn) {
        return notificationRepository.findAllByUser_UserSn(userSn);
    }

    @Operation(summary = "현재 사용자 맞춤 알림 목록 조회 (매칭 기반)", description = "현재 로그인된 사용자의 관심사에 맞는 다른 사용자 매칭 결과를 알림 형태로 반환합니다. (세션 기반)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", implementation = NotificationDto.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음 (테스트용 임시 로직)", content = @Content)
    })
    @PostMapping("/user-matching")
    public List<NotificationDto> getNotificationList(HttpSession session) {
       User user = (User) session.getAttribute("user_info");
        // Optional<User> opt = userRepository.findByEmailAndUserPw("test@naver.com", "12345"); // 유저 센션값 가져오면 삭제가능
        // User user = opt.get(); //이거 두줄 없애고 맨 위에 줄 살리면 로그인 유저 받아옴
        List<Notification> matchingUsers = userMatchService.findMatchingUsers(user);

        return matchingUsers.stream()
                .map(NotificationDto::new)
                .toList();

    }


}