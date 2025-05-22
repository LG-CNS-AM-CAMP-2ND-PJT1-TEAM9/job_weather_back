package com.example.job_weather_back.controller;

import com.example.job_weather_back.dto.NotificationDto;
import com.example.job_weather_back.entity.Notification;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.NotificationRepository;
import com.example.job_weather_back.repository.UserRepository;
import com.example.job_weather_back.service.UserMatchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserMatchService userMatchService;

    // 모든 알림 조회
    @GetMapping
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    // 특정 사용자 알림만 조회
    @GetMapping("/user/{userSn}")
    public List<Notification> getByUser(@PathVariable Integer userSn) {
        return notificationRepository.findAllByUser_UserSn(userSn);
    }

    @PostMapping("/user-matching")
    public List<NotificationDto> getNotificationList(@RequestBody User requestedUser, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user_info");
        // session에 저장되어 있는 사용자와 실제 넘어온 사용자가 다른 경우
        if (sessionUser == null || requestedUser.getUserSn() != sessionUser.getUserSn()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        Optional<User> opt = userRepository.findByEmailAndUserPw(sessionUser.getEmail(), sessionUser.getUserPw());
        if (opt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        List<Notification> matchingUsers = userMatchService.findMatchingUsers(opt.get());

        return matchingUsers.stream().map(NotificationDto::new).toList();
    }

}