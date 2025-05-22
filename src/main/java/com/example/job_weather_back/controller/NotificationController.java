package com.example.job_weather_back.controller;

import com.example.job_weather_back.dto.NotificationDto;
import com.example.job_weather_back.entity.Notification;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.NotificationRepository;
import com.example.job_weather_back.repository.UserRepository;
import com.example.job_weather_back.service.UserMatchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public List<NotificationDto> getNotificationList(HttpSession session) {
//        User user = (User) session.getAttribute("user_info");
        Optional<User> opt = userRepository.findByEmailAndUserPw("test@naver.com", "12345"); // 유저 센션값 가져오면 삭제가능
        User user = opt.get(); //이거 두줄 없애고 맨 위에 줄 살리면 로그인 유저 받아옴
        List<Notification> matchingUsers = userMatchService.findMatchingUsers(user);

        return matchingUsers.stream()
                .map(NotificationDto::new)
                .toList();

    }


}