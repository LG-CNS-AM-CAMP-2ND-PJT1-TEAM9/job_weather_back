package com.example.job_weather_back.service;

import com.example.job_weather_back.entity.Customization;
import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.entity.Notification;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.CustomizationRepository;
import com.example.job_weather_back.repository.NewContentsRepository;
import com.example.job_weather_back.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMatchService {

    private final CustomizationRepository customizationRepository;
    private final NewContentsRepository newContentsRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public List<Notification> findMatchingUsers(User user) {
        // 1. 사용자에 해당하는 customization 데이터 가져오기 (customization 테이블에 데이터 없음)
        List<Customization> userCustomizations = customizationRepository.findByUser(user);

        // 매칭된 콘텐츠를 저장할 Set (중복 방지)
        Set<NewContents> matchedContents = new HashSet<>();

        // 2. 각 커스터마이제이션 설정에 대해 매칭되는 콘텐츠 찾기
        for (Customization customization : userCustomizations) {
            // 포지션, 위치, 회사 타입 정보 가져오기
            if (customization.getCustomPosition() != null) {
                String positionName = customization.getCustomPosition().getPositionName();
                matchedContents.addAll(newContentsRepository.searchByKeyword(positionName));
            }

            if (customization.getCustomLocation() != null) {
                String locationName = customization.getCustomLocation().getLocationName();
                matchedContents.addAll(newContentsRepository.searchByKeyword(locationName));
            }

            if (customization.getCompanyType() != null) {
                String typeName = customization.getCompanyType().getTypeName();
                matchedContents.addAll(newContentsRepository.searchByKeyword(typeName));
            }
        }

        // 3. 이미 알림이 생성된 콘텐츠 필터링
        List<Notification> existingNotifications = notificationRepository.findByUser(user);
        Set<NewContents> existingContents = existingNotifications.stream()
                .map(Notification::getNewContents)
                .collect(Collectors.toSet());

        // 이미 알림이 생성된 콘텐츠 제외
        matchedContents.removeAll(existingContents);

        // 4. 새로운 알림 생성 및 저장
        List<Notification> newNotifications = new ArrayList<>();
        for (NewContents content : matchedContents) {
            Notification notification = new Notification();
            notification.setNewContents(content);
            notification.setUser(user);
            notification.setIsRead(Boolean.FALSE);
            notification.setCreatedAt(LocalDateTime.now());

            notificationRepository.save(notification);
            newNotifications.add(notification);
        }

        // 5. 모든 알림 반환 (기존 + 새로 생성된 알림)
        return notificationRepository.findAllWithRelations(user.getUserSn());
    }
}
