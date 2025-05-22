package com.example.job_weather_back.service;

import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.entity.Notification;
import com.example.job_weather_back.entity.UserMatchSetting;
import com.example.job_weather_back.repository.NewContentsRepository;
import com.example.job_weather_back.repository.NotificationRepository;
import com.example.job_weather_back.repository.UserMatchSettingRepository;
import com.example.job_weather_back.util.KeywordExtractor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewContentsService {

    private final NewContentsRepository newContentsRepository;
    private final NotificationRepository notificationRepository;
    private final UserMatchSettingRepository userMatchSettingRepository;
    private final KeywordExtractor keywordExtractor;

    @Transactional
    public void saveNewContents(String type, String title, String content) {
        // 1. 소식 저장
        NewContents newContent = new NewContents();
        newContent.setType(type); // "news" 또는 "job"
        newContent.setTitle(title);
        newContent.setContent(content);
        newContent.setCreatedAt(LocalDateTime.now());
        newContentsRepository.save(newContent);

        // 2. 키워드 3개 추출
        List<String> top3Keywords = keywordExtractor.extractTop3AsStringList(title + " " + content);

        // 3. 사용자 설정 가져와서 키워드 비교 후 알림 생성
        List<UserMatchSetting> settings = userMatchSettingRepository.findAll();

        for (UserMatchSetting setting : settings) {
            Set<String> userKeywords = convertUserSettingToKeywordNames(setting);

            boolean matched = top3Keywords.stream().anyMatch(userKeywords::contains);
            if (matched) {
                Notification notification = new Notification();
                notification.setUser(setting.getUser());
//                notification.setAlertType(type);
//                notification.setTitle(title);
//                notification.setContent(content);
                notification.setIsRead(false);
                notification.setCreatedAt(LocalDateTime.now());

                notificationRepository.save(notification);
            }
        }
    }

    private Set<String> convertUserSettingToKeywordNames(UserMatchSetting setting) {
        Set<String> all = new HashSet<>();

        if (setting.getTypeIds() != null) {
            setting.getTypeIds().forEach(id -> all.add(String.valueOf(id)));
        }
        if (setting.getPositionIds() != null) {
            setting.getPositionIds().forEach(id -> all.add(String.valueOf(id)));
        }
        if (setting.getLocationIds() != null) {
            setting.getLocationIds().forEach(id -> all.add(String.valueOf(id)));
        }
        if (setting.getCompanyTypeIds() != null) {
            setting.getCompanyTypeIds().forEach(id -> all.add(String.valueOf(id)));
        }

        return all;
    }
} 